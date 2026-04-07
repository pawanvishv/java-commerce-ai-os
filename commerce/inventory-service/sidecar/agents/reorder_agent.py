from pydantic import BaseModel
from typing import List, Optional


class InventoryItem(BaseModel):
    sku: str
    available_qty: int
    reserved_qty: int
    avg_daily_sales: Optional[float] = 0
    lead_time_days: Optional[int] = 7


class ReorderRecommendation(BaseModel):
    sku: str
    current_qty: int
    recommended_reorder_qty: int
    urgency: str
    reason: str


class ReorderAgent:
    """
    AI agent for reorder recommendations.
    Rule-based fallback: reorder when qty < 2x lead time demand
    Circuit breaker: 50% failure over 10 calls -> OPEN
    """

    def __init__(self):
        self.failure_count = 0
        self.call_count = 0
        self.circuit_open = False

    def recommend(self,
                  items: List[InventoryItem]
                  ) -> List[ReorderRecommendation]:
        self.call_count += 1

        if self.circuit_open:
            return self._rule_based_recommend(items)

        try:
            result = self._ai_recommend(items)
            return result
        except Exception as e:
            self.failure_count += 1
            if self.call_count >= 10 and \
               self.failure_count / self.call_count >= 0.5:
                self.circuit_open = True
                print(f"Circuit breaker OPEN: {e}")
            return self._rule_based_recommend(items)

    def _ai_recommend(self,
                      items: List[InventoryItem]
                      ) -> List[ReorderRecommendation]:
        return self._rule_based_recommend(items)

    def _rule_based_recommend(self,
                               items: List[InventoryItem]
                               ) -> List[ReorderRecommendation]:
        recommendations = []
        for item in items:
            safety_stock = (
                item.avg_daily_sales * item.lead_time_days * 2
            )
            if item.available_qty <= safety_stock:
                urgency = "HIGH" if item.available_qty == 0 \
                    else "MEDIUM"
                reorder_qty = max(
                    int(item.avg_daily_sales * 30), 10
                )
                recommendations.append(
                    ReorderRecommendation(
                        sku=item.sku,
                        current_qty=item.available_qty,
                        recommended_reorder_qty=reorder_qty,
                        urgency=urgency,
                        reason=f"Stock below safety level "
                               f"({safety_stock:.0f} units)"
                    )
                )
        return recommendations
