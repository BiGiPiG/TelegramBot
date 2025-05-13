from typing import Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field
from g4f.client import Client
import logging

# logging.basicConfig(level=logging.INFO)
# logger = logging.getLogger(__name__)

app = FastAPI()


class GlobalQuote(BaseModel):
    symbol: str = Field(..., alias="01. symbol")
    open: str = Field(..., alias="02. open")
    high: str = Field(..., alias="03. high")
    low: str = Field(..., alias="04. low")
    price: str = Field(..., alias="05. price")
    volume: str = Field(..., alias="06. volume")
    latest_trading_day: str = Field(..., alias="07. latest trading day")
    previous_close: str = Field(..., alias="08. previous close")
    change: str = Field(..., alias="09. change")
    change_percent: str = Field(..., alias="10. change percent")

    model_config = {
        "populate_by_name": True,
        "validate_assignment": True
    }


class Share(BaseModel):
    symbol: str = Field(..., alias="Symbol")
    name: str = Field(..., alias="Name")
    description: str = Field(..., alias="Description")
    eps: float = Field(..., alias="EPS")
    ps_ratio: float = Field(..., alias="PriceToSalesRatioTTM")
    market_capitalization: float = Field(..., alias="MarketCapitalization")
    diluted_eps_ttm: float = Field(..., alias="DilutedEPSTTM")
    book_value: float = Field(..., alias="BookValue")
    sector: str = Field(..., alias="Sector")
    global_quote: GlobalQuote = Field(..., alias="Global Quote")
    pb_ratio: float = Field(..., alias="PriceToBookRatio")
    pe_ratio: float = Field(..., alias="PERatio")
    industry: Optional[str] = Field(None, alias="Industry")  # Если поле может быть пустым
    country: Optional[str] = Field(None, alias="Country")  # Если поле может быть пустым
    ebitda: Optional[float] = Field(None, alias="EBITDA")
    revenue: Optional[float] = Field(None, alias="RevenueTTM")
    gross_profit: Optional[float] = Field(None, alias="GrossProfitTTM")
    dividend_yield: Optional[float] = Field(None, alias="DividendYield")
    return_on_equity: Optional[float] = Field(None, alias="ReturnOnEquityTTM")
    profit_margin: Optional[float] = Field(None, alias="ProfitMargin")
    peg_ratio: Optional[float] = Field(None, alias="PEGRatio")
    ev_to_ebitda: Optional[float] = Field(None, alias="EVToEBITDA")
    analyst_target_price: Optional[float] = Field(None, alias="AnalystTargetPrice")

    model_config = {
        "populate_by_name": True,
        "validate_assignment": True
    }


@app.post("/smartAnalyze/")
async def create_item(share: Share):
    # logger.info("Запрос обрабатывается")
    analyze = get_analyze(share)
    # logger.info("Обработка завершена")
    return analyze


def get_analyze(share_to_analyze: Share) -> str:
    client = Client()
    prompt = f"""
            Проанализируй акцию {share_to_analyze.name} на основе предоставленных данных и дай инвестиционный совет. 
            Учти следующие аспекты:

            ### Текущая ситуация на рынке
            - **Цена акции**: ${share_to_analyze.global_quote.price}
            - **Ежедневные метрики**:
              - **Максимальная цена**: ${share_to_analyze.global_quote.high}
              - **Минимальная цена**: ${share_to_analyze.global_quote.low}
              - **Изменение цены**: {share_to_analyze.global_quote.change} ({share_to_analyze.global_quote
                                     .change_percent})
              - **Объем торгов**: {share_to_analyze.global_quote.volume} акций

            ### Фундаментальные метрики
            - **P/E (Цена/Прибыль)**: {share_to_analyze.pe_ratio}
            - **P/B (Цена/Книжная стоимость)**: {share_to_analyze.pb_ratio}
            - **P/S (Цена/Продажи)**: {share_to_analyze.ps_ratio}
            - **Рыночная капитализация**: ${share_to_analyze.market_capitalization}
            - **EPS (Прибыль на акцию)**: ${share_to_analyze.eps}
            - **Книжная стоимость акции**: ${share_to_analyze.book_value}

            ### Общая информация
            - **Тикер**: {share_to_analyze.symbol}
            - **Описание**: {share_to_analyze.description}
            - **Сектор**: {share_to_analyze.sector}
            - **Отрасль**: {share_to_analyze.industry}  # Добавлено из нового DTO
            - **Страна**: {share_to_analyze.country}  # Добавлено из нового DTO

            ### Вопросы для анализа
            1. **Технический анализ**:
               - Сравни текущую цену с дневным high/low. Есть ли признаки перекупленности/перепроданности?
               - Какой потенциал роста/падения цены акции на основе текущих данных?

            2. **Фундаментальный анализ**:
               - Сравни мультипликаторы (P/E, P/B, P/S) со средними значениями по отрасли. Переоценена ли акция?
               - Как влияет высокая рыночная капитализация на потенциал роста компании?

            3. **Общий вывод и рекомендация**:
               - Оцени потенциал роста/падения акции на основе данных.
               - Дай совет: покупать или нет, держать или продавать с обоснованием.
               - Упомяни ключевые риски (например, высокий P/B, волатильность рынка, влияние отраслевых факторов).

            Ответ оформи кратко, с выделением главных тезисов **жирным**. Не используй форматирование, можешь только 
            выделить **жирным** или добавить смайлик.
        """

    response = client.chat.completions.create(
        model="gpt-4",
        messages=[{"role": "user", "content": prompt}],
        web_search=False
    )

    return response.choices[0].message.content


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
