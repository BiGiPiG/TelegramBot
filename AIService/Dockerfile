FROM python:3.9-slim

# Устанавливаем переменные окружения
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=off

WORKDIR /app

COPY requirements.txt /app/

RUN pip install --no-cache-dir -r requirements.txt

COPY . /app/

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]