using System.Collections.Generic;

namespace ComposableCrypto.Core
{
    public class Or : Condition
    {
        public Condition Condition1 { get; }
        public Condition Condition2 { get; }

        public Or(Condition condition1, Condition condition2)
        {
            Condition1 = condition1;
            Condition2 = condition2;
        }

        public static Condition Of(params Condition[] conditions)
        {
            Condition result = conditions[0];
            for (int i = 1; i < conditions.Length; i++)
            {
                result = new Or(result, conditions[i]);
            }
            return result;
        }

        public static Condition Of(IEnumerable<Condition> conditions)
        {
            IEnumerator<Condition> enumerator = conditions.GetEnumerator();
            if (!enumerator.MoveNext())
            {
                return TRUE;
            }
            Condition result = enumerator.Current;
            while (enumerator.MoveNext())
            {
                result = new Or(result, enumerator.Current);
            }
            return result;
        }

        public override bool Evaluate(Component component)
        {
            return Condition1.Evaluate(component) || Condition2.Evaluate(component);
        }

        public override string ToString()
        {
            return $"({Condition1} ∨ {Condition2})";
        }

        public override string ToHighlightedString(Component component)
        {
            return $"({Condition1.ToHighlightedString(component)} ∨ {Condition2.ToHighlightedString(component)})";
        }
    }
}
