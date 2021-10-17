using System;
using System.Collections.Generic;
using System.Text;

namespace ComposableCrypto.Core
{
    public static class PropositionalLogicUtils
    {
        public static Condition ToNNF(Condition condition) => condition switch
        {
            Not not when not.Condition is Not => not.Condition,
            Not not when not.Condition is And conjunction => new Or(ToNNF(new Not(conjunction.Condition1)), ToNNF(new Not(conjunction.Condition2))),
            Not not when not.Condition is Or disjunction => new And(ToNNF(new Not(disjunction.Condition1)), ToNNF(new Not(disjunction.Condition2))),
            And conjunction => new And(ToNNF(conjunction.Condition1), ToNNF(conjunction.Condition2)),
            Or disjunction => new Or(ToNNF(disjunction.Condition1), ToNNF(disjunction.Condition2)),
            _ => condition,
        };

        public static Condition ToDNF(Condition condition) => ToDNFHelper(ToNNF(condition));

        private static Condition ToDNFHelper(Condition condition) => condition switch
        {
            And conjunction when conjunction.Condition2 is Or disjunction => new Or(ToDNFHelper(new And(conjunction.Condition1, disjunction.Condition1)), ToDNFHelper(new And(conjunction.Condition1, disjunction.Condition2))),
            And conjunction when conjunction.Condition1 is Or disjunction => new Or(ToDNFHelper(new And(disjunction.Condition1, conjunction.Condition2)), ToDNFHelper(new And(disjunction.Condition2, conjunction.Condition2))),
            Or disjunction => new Or(ToDNFHelper(disjunction.Condition1), ToDNFHelper(disjunction.Condition2)),
            _ => condition,
        };

        public static IList<Condition> CollectAlternatives(Condition condition)
        {
            condition = ToDNF(condition);
            IList<Condition> alternatives = new List<Condition>();
            CollectAlternativesHelper(condition, alternatives);
            return alternatives;
        }

        private static void CollectAlternativesHelper(Condition condition, IList<Condition> alternatives)
        {
            switch (condition)
            {
                case Or disjunction:
                    CollectAlternativesHelper(disjunction.Condition1, alternatives);
                    CollectAlternativesHelper(disjunction.Condition2, alternatives);
                    break;
                default:
                    alternatives.Add(condition);
                    break;
            }
        }

        public static (IDictionary<string, ICollection<string>> requiredProperties, IDictionary<string, ICollection<string>> requiredNegProperties) CollectRequiredChildrenProperties(Condition condition)
        {
            IDictionary<string, ICollection<string>> requiredProps = new Dictionary<string, ICollection<string>>();
            IDictionary<string, ICollection<string>> requiredNegProps = new Dictionary<string, ICollection<string>>();
            CollectRequiredChildrenPropertiesHelper(condition, requiredProps, requiredNegProps);
            return (requiredProps, requiredNegProps);
        }

        private static void CollectRequiredChildrenPropertiesHelper(Condition condition, IDictionary<string, ICollection<string>> requiredProperties, IDictionary<string, ICollection<string>> requiredNegProperties)
        {
            switch (condition)
            {
                case And conjunction:
                    CollectRequiredChildrenPropertiesHelper(conjunction.Condition1, requiredProperties, requiredNegProperties);
                    CollectRequiredChildrenPropertiesHelper(conjunction.Condition2, requiredProperties, requiredNegProperties);
                    break;
                case Or:
                    throw new ArgumentException("This method does not accept conditions with disjunctions.");
                case Not not:
                    if (not.Condition is not ChildPropertyCondition && not.Condition is not AssumptionCondition)
                    {
                        throw new ArgumentException("This method only accepts conditions in NNF without disjunctions.");
                    }
                    CollectRequiredChildrenPropertiesHelper(not.Condition, requiredNegProperties, null);
                    break;
                case ChildPropertyCondition childPropertyCondition:
                    if (!requiredProperties.TryGetValue(childPropertyCondition.ChildName, out ICollection<string> props))
                    {
                        props = new HashSet<string>();
                        requiredProperties.Add(childPropertyCondition.ChildName, props);
                    }
                    props.Add(childPropertyCondition.PropertyName);
                    break;
                default:
                    break;
            }
        }
    }
}
