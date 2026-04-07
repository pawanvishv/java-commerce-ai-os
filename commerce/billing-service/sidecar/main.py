from fastapi import FastAPI, HTTPException
from agents.invoice_agent import InvoiceAgent, InvoiceRequest

app = FastAPI(title="Billing AI Sidecar")
agent = InvoiceAgent()


@app.get("/health")
def health():
    return {
        "status": "UP",
        "circuit_open": agent.circuit_open,
        "failure_rate": (
            agent.failure_count / agent.call_count
            if agent.call_count > 0 else 0
        )
    }


@app.post("/invoice/generate")
def generate_invoice(request: InvoiceRequest):
    try:
        html = agent.generate_invoice_html(request)
        return {"html": html, "fallback": agent.circuit_open}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8200)
