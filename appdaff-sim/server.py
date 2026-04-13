#!/usr/bin/env python3
import json
import os
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, unquote


def load_fixtures():
  here = os.path.dirname(os.path.abspath(__file__))
  fixtures_path = os.path.join(here, "fixtures", "users.json")
  with open(fixtures_path, "r", encoding="utf-8") as f:
    data = json.load(f)
  users = data.get("users", [])
  by_cni = {}
  for u in users:
    primary = str(u.get("cni", "")).strip()
    if primary:
      by_cni[primary] = u
    aliases = u.get("aliases", [])
    if isinstance(aliases, list):
      for a in aliases:
        alias = str(a or "").strip()
        if alias:
          by_cni[alias] = u
  return users, by_cni


USERS, USERS_BY_CNI = load_fixtures()


class Handler(BaseHTTPRequestHandler):
  def _send_json(self, status, payload):
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    self.send_response(status)
    self.send_header("Content-Type", "application/json; charset=utf-8")
    self.send_header("Content-Length", str(len(body)))
    self.end_headers()
    self.wfile.write(body)

  def do_GET(self):
    parsed = urlparse(self.path)
    path = parsed.path.rstrip("/")

    if path == "/api/v1/health":
      return self._send_json(200, {"status": "ok"})

    if path == "/api/v1/citoyens":
      return self._send_json(200, {"users": USERS})

    prefix = "/api/v1/citoyens/"
    if path.startswith(prefix):
      cni = unquote(path[len(prefix):]).strip()
      user = USERS_BY_CNI.get(cni)
      if not user:
        return self._send_json(404, {"error": "CNI inconnu"})
      return self._send_json(200, user)

    return self._send_json(404, {"error": "Not Found"})

  def log_message(self, fmt, *args):
    # Keep logs minimal/clean
    return


def main():
  host = os.environ.get("APPDAFF_HOST", "0.0.0.0")
  port = int(os.environ.get("APPDAFF_PORT", "9090"))
  httpd = HTTPServer((host, port), Handler)
  print(f"[appdaff-sim] listening on http://{host}:{port}", flush=True)
  httpd.serve_forever()


if __name__ == "__main__":
  main()
