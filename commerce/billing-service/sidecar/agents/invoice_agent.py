from pydantic import BaseModel
from typing import List, Optional
import re


class InvoiceLine(BaseModel):
    sku: str
    item_name: str
    qty: int
    unit_price_paise: int
    tax_paise: int
    total_paise: int


class InvoiceRequest(BaseModel):
    order_id: str
    tenant_id: str
    customer_name: Optional[str] = "Valued Customer"
    lines: List[InvoiceLine]
    total_paise: int
    tax_paise: int


class InvoiceAgent:
    """
    AI agent for generating invoice HTML.
    Circuit breaker: 50% failure over 10 calls -> OPEN
    Falls back to rule-based generator.
    """

    def __init__(self):
        self.failure_count = 0
        self.call_count = 0
        self.circuit_open = False

    def generate_invoice_html(self, request: InvoiceRequest) -> str:
        self.call_count += 1

        if self.circuit_open:
            return self._fallback_invoice(request)

        try:
            html = self._generate_html(request)
            self.failure_count = max(0, self.failure_count - 1)
            return html
        except Exception as e:
            self.failure_count += 1
            if self.call_count >= 10 and \
               self.failure_count / self.call_count >= 0.5:
                self.circuit_open = True
                print(f"Circuit breaker OPEN: {e}")
            return self._fallback_invoice(request)

    def _generate_html(self, request: InvoiceRequest) -> str:
        lines_html = ""
        for line in request.lines:
            lines_html += f"""
            <tr>
                <td>{line.sku}</td>
                <td>{line.item_name}</td>
                <td>{line.qty}</td>
                <td>₹{line.unit_price_paise / 100:.2f}</td>
                <td>₹{line.tax_paise / 100:.2f}</td>
                <td>₹{line.total_paise / 100:.2f}</td>
            </tr>"""

        return f"""
        <!DOCTYPE html>
        <html>
        <head><title>Invoice - {request.order_id}</title></head>
        <body>
            <h1>INVOICE</h1>
            <p>Order: {request.order_id}</p>
            <p>Customer: {request.customer_name}</p>
            <table border="1">
                <tr>
                    <th>SKU</th><th>Item</th><th>Qty</th>
                    <th>Unit Price</th><th>Tax</th><th>Total</th>
                </tr>
                {lines_html}
            </table>
            <p><strong>Tax: ₹{request.tax_paise / 100:.2f}</strong></p>
            <p><strong>Total: ₹{request.total_paise / 100:.2f}</strong></p>
        </body>
        </html>"""

    def _fallback_invoice(self, request: InvoiceRequest) -> str:
        return f"""
        <html><body>
        <h1>Invoice {request.order_id}</h1>
        <p>Total: Rs.{request.total_paise / 100:.2f}</p>
        </body></html>"""
