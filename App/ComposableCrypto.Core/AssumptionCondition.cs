namespace ComposableCrypto.Core
{
    public class AssumptionCondition : Condition
    {
        public string Assumption { get; }

        public AssumptionCondition(string assumption)
        {
            Assumption = assumption;
        }

        public override bool Evaluate(Component component)
        {
            return AssumptionRegistry.Evaluate(Assumption);
        }

        public override string ToString()
        {
            return Assumption;
        }

        public override string ToHighlightedString(Component component)
        {
            if (Evaluate(component))
            {
                return $"<span class=\"bg-success text-white\">{ToString()}</span>";
            }
            else
            {
                return $"<span class=\"bg-danger text-white\">{ToString()}</span>";
            }
        }
    }
}
