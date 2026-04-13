FROM python:3.12-alpine
WORKDIR /app
COPY server.py /app/server.py
COPY fixtures /app/fixtures
ENV APPDAFF_PORT=9090
EXPOSE 9090
CMD ["python3", "/app/server.py"]

