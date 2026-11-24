#!/usr/bin/env python3
"""Test de bout en bout de l'interface (Chrome sans interface, Playwright) contre l'API en profil postgres.
Produit des captures horodatées dans /tmp/evidence/ui/ et un journal ui_e2e.csv."""
import subprocess, datetime, os, sys, json
from playwright.sync_api import sync_playwright

UI = "http://localhost:4173"
SHOTS = os.environ.get("UI_SHOTS", "/tmp/naatalvote-ui"); os.makedirs(SHOTS, exist_ok=True)
rows = []

def sql(q):
    return subprocess.run(["docker", "exec", "naatalvote-esp221-postgres", "psql", "-U", "naatalvote", "-d", "naatalvote", "-At", "-c", q], capture_output=True, text=True, check=True).stdout.strip()

def rec(tid, obj, attendu, observe, ok, shot=""):
    rows.append([tid, obj, attendu, observe, "Réussi" if ok else "Échec", shot,
                 datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")])
    print(f"{tid} {'OK' if ok else 'KO'} | {obj} | attendu={attendu} | observé={observe}")

# Élection de démonstration ouverte maintenant (les élections de démo de 2026 sont closes)
admin = sql("select utilisateur_id from utilisateur_telephone where telephone='+221770000002'")
title = "Municipales Dakar (démonstration) " + datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%d%H%M%S")
eid = sql(f"""insert into election(id,titre,description,type_election,date_debut,date_fin,statut,admin_id,region,total_electeurs)
 values (gen_random_uuid(),'{title}','Scrutin de démonstration','MUNICIPALE',now()-interval '1 hour',now()+interval '2 days','EN_COURS','{admin}','Dakar',1200) returning id""").splitlines()[0]
for nom, prenom, parti, color in (("Diallo", "Aminata", "Union pour Dakar", "#1F5A33"), ("Sarr", "Moussa", "Dakar Avenir", "#264C8C"), ("Faye", "Khady", "Renouveau citoyen", "#8A5A10")):
    sql(f"insert into candidat(id,election_id,nom,prenom,parti_politique,biographie,photo_url,programme_url,color) values (gen_random_uuid(),'{eid}','{nom}','{prenom}','{parti}','Candidat fictif','','','{color}')")

with sync_playwright() as p:
    b = p.chromium.launch(executable_path="/usr/bin/google-chrome", headless=True, args=["--no-sandbox"])
    page = b.new_page(viewport={"width": 1366, "height": 860})
    console = []
    page.on("console", lambda m: console.append(m.text) if m.type == "error" else None)

    # UI-01 garde de navigation sans session
    page.goto(f"{UI}/admin/dashboard"); page.wait_for_timeout(1200)
    shot = f"{SHOTS}/ui01_garde_sans_session.png"; page.screenshot(path=shot)
    rec("UI-01", "Accès direct à /admin/dashboard sans session", "redirection /login", page.url.replace(UI, ""), page.url.endswith("/login"), shot)

    # UI-02 connexion citoyen : numéros masqués + OTP
    page.goto(f"{UI}/login"); page.wait_for_timeout(800)
    page.fill("#cni", "20119990214000017"); page.wait_for_function("Array.from(document.querySelectorAll('#phone option')).some(o => o.textContent.includes('*'))", timeout=15000)
    opts = page.eval_on_selector_all("#phone option", "els => els.map(e => e.textContent.trim()).filter(Boolean)")
    shot = f"{SHOTS}/ui02_numeros_masques.png"; page.screenshot(path=shot)
    rec("UI-02", "Saisie de la CNI : liste des numéros proposés", "numéros masqués", "; ".join(opts), any("*" in o for o in opts) and not any("770000001" in o for o in opts), shot)
    masked = [o for o in opts if "*" in o]
    if masked:
        page.select_option("#phone", label=masked[0])
    page.click("button[type=submit]"); page.wait_for_timeout(1500)
    otp_txt = page.inner_text("body")
    import re
    m = re.search(r"\b(\d{6})\b", otp_txt)
    shot = f"{SHOTS}/ui03_otp_demo.png"; page.screenshot(path=shot)
    rec("UI-03", "Fenêtre OTP (mode démonstration)", "code à 6 chiffres affiché", m.group(1) if m else "absent", bool(m), shot)
    if m:
        page.locator("#popup-otp").press_sequentially(m.group(1), delay=40); page.wait_for_timeout(300)
        page.get_by_text("Valider le code").first.click(); page.wait_for_timeout(2500)
    shot = f"{SHOTS}/ui04_tableau_de_bord_citoyen.png"; page.screenshot(path=shot)
    rec("UI-04", "Après vérification OTP", "/citoyen/dashboard", page.url.replace(UI, ""), "/citoyen/dashboard" in page.url, shot)

    # UI-05 un citoyen ne peut pas ouvrir l'espace super administrateur
    page.goto(f"{UI}/superadmin/console"); page.wait_for_timeout(1200)
    shot = f"{SHOTS}/ui05_garde_role.png"; page.screenshot(path=shot)
    rec("UI-05", "Citoyen connecté -> /superadmin/console", "redirection /portal", page.url.replace(UI, ""), page.url.endswith("/portal"), shot)

    # UI-06 parcours de vote
    page.goto(f"{UI}/citoyen/vote"); page.wait_for_timeout(2500)
    shot = f"{SHOTS}/ui06_page_vote.png"; page.screenshot(path=shot, full_page=False)
    screens = 1
    page.get_by_role("button").filter(has_text=title).click()
    page.get_by_role("heading", name=title, exact=True).wait_for()
    page.wait_for_timeout(1200)
    vote_buttons = page.locator("button", has_text="Voter")
    n_btn = vote_buttons.count()
    if n_btn:
        vote_buttons.first.click(); page.wait_for_timeout(1000); screens += 1
        page.screenshot(path=f"{SHOTS}/ui07_confirmation_1.png")
        if page.locator(".swal-confirm").count(): page.locator(".swal-confirm").first.click(); page.wait_for_timeout(1000); screens += 1
        page.screenshot(path=f"{SHOTS}/ui08_confirmation_2.png")
        if page.locator(".swal-confirm").count(): page.locator(".swal-confirm").first.click(); page.wait_for_timeout(3500); screens += 1
        page.screenshot(path=f"{SHOTS}/ui09_vote_enregistre.png")
        if page.locator(".swal-confirm").count(): page.locator(".swal-confirm").first.click(); page.wait_for_timeout(2000); screens += 1
    shot = f"{SHOTS}/ui10_recu.png"; page.screenshot(path=shot, full_page=True)
    nb = sql(f"select count(*) from vote where election_id='{eid}'")
    traces = sql(f"select count(*) from trace_vote where election_id='{eid}'")
    rec("UI-06", "Parcours de vote complet dans l'interface", "1 bulletin + 1 trace ; ≤ 5 écrans", f"bulletins={nb}, traces={traces}, écrans/étapes={screens}, URL finale={page.url.replace(UI,'')}",
        nb == "1" and traces == "1" and screens <= 5, shot)

    # UI-07 second vote refusé dans l'interface
    page.goto(f"{UI}/citoyen/vote"); page.wait_for_timeout(2500)
    page.get_by_role("button").filter(has_text=title).click()
    page.get_by_role("heading", name=title, exact=True).wait_for()
    page.wait_for_timeout(1000)
    body = page.inner_text("body")
    shot = f"{SHOTS}/ui11_deja_vote.png"; page.screenshot(path=shot)
    already = any(k in body.lower() for k in ("déjà voté", "deja vote", "vote enregistré", "a déjà"))
    rec("UI-07", "Retour sur la page de vote après avoir voté", "indication « déjà voté » ou vote impossible", "indication présente" if already else "aucune indication", already, shot)

    # UI-08 responsive (mobile)
    mob = b.new_page(viewport={"width": 390, "height": 844})
    mob.goto(f"{UI}/login"); mob.wait_for_timeout(1200)
    shot = f"{SHOTS}/ui12_mobile_login.png"; mob.screenshot(path=shot)
    overflow = mob.evaluate("document.documentElement.scrollWidth > window.innerWidth + 2")
    rec("UI-08", "Page de connexion sur écran mobile 390 px", "pas de défilement horizontal", "débordement" if overflow else "aucun débordement", not overflow, shot)

    # UI-09 journal du super administrateur (signature absente -> pas de plantage, affichage « — »)
    sp = b.new_page(viewport={"width": 1366, "height": 860})
    errs = []
    sp.on("pageerror", lambda e: errs.append(str(e)))
    sp.goto(f"{UI}/login"); sp.wait_for_timeout(800)
    sp.fill("#cni", "10419881201000040"); sp.wait_for_function("Array.from(document.querySelectorAll('#phone option')).some(o => o.textContent.includes('*'))", timeout=15000)
    o = [x for x in sp.eval_on_selector_all("#phone option", "els => els.map(e => e.textContent.trim())") if "*" in x]
    sp.select_option("#phone", label=o[0]); sp.click("button[type=submit]"); sp.wait_for_timeout(1500)
    code = re.search(r"\b(\d{6})\b", sp.inner_text("body")).group(1)
    sp.locator("#popup-otp").press_sequentially(code, delay=40); sp.get_by_text("Valider le code").first.click(); sp.wait_for_timeout(2500)
    if "/portal" in sp.url:
        sp.locator("button", has_text="Super Administrateur").first.click(); sp.wait_for_timeout(1500)
    sp.goto(f"{UI}/superadmin/logs"); sp.wait_for_timeout(2500)
    shot = f"{SHOTS}/ui13_journal_superadmin.png"; sp.screenshot(path=shot)
    rec("UI-09", "Page Journal du super administrateur (signatures absentes)", "affichage sans erreur JavaScript", f"URL={sp.url.replace(UI,'')}, erreurs={len(errs)}", "/superadmin/logs" in sp.url and not errs, shot)
    sp.goto(f"{UI}/citoyen/vote/confirm"); sp.wait_for_timeout(1000)
    rec("UI-10", "Ancienne page de vote simulée /citoyen/vote/confirm", "absente (aucun écran de vote fictif)", sp.url.replace(UI, ""), "vote/confirm" not in sp.url or "Vote enregistr" not in sp.inner_text("body"), "")

    ext = [c for c in console if "ERR_TUNNEL_CONNECTION_FAILED" in c]
    rec("UI-LOG", "Erreurs en console pendant le parcours (hors ressources externes bloquées par le réseau de test)", "0", f"{len(console) - len(ext)} (+{len(ext)} ressources CDN non chargées)", len(console) - len(ext) == 0, "")
    b.close()

with open(os.environ.get("UI_REPORT", "/tmp/naatalvote-ui.csv"), "w") as f:
    f.write("id;objectif;attendu;observé;statut;capture;horodatage\n")
    for r in rows: f.write(";".join(str(x).replace(";", ",") for x in r) + "\n")
print(json.dumps(console[:5], ensure_ascii=False))

if not all(r[4] == "Réussi" for r in rows): sys.exit(1)
