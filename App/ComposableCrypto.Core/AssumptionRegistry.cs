using System.Collections.Generic;

namespace ComposableCrypto.Core
{
    public class AssumptionRegistry
    {
        private static readonly Dictionary<string, bool?> customAssumptions = new Dictionary<string, bool?>();
        private static readonly List<AssumptionProfile> assumptionProfiles = new List<AssumptionProfile>();

        public static void RegisterAssumption(string name, bool? value = null)
        {
            _ = customAssumptions.TryAdd(name, value);
        }

        public static void RegisterAssumptionProfile(AssumptionProfile profile)
        {
            assumptionProfiles.Add(profile);
        }

        public static void RegisterAssumptionProfile(string json)
        {
            assumptionProfiles.Add(AssumptionProfile.FromJson(json));
        }

        public static bool Evaluate(string name)
        {
            bool? customValue = customAssumptions[name];
            if (customValue.HasValue)
            {
                return customValue.Value;
            }

            foreach (AssumptionProfile profile in assumptionProfiles)
            {
                if (profile.Assumptions.TryGetValue(name, out bool value))
                {
                    return value;
                }
            }
            return false;
        }

        public static IDictionary<string, bool?> Assumptions => customAssumptions;
        public static IList<AssumptionProfile> AssumptionProfiles => assumptionProfiles.AsReadOnly();
        public static int AssumptionProfileCount => assumptionProfiles.Count;

        public static void SwapAssumptionProfiles(int index1, int index2)
        {
            AssumptionProfile temp = assumptionProfiles[index1];
            assumptionProfiles[index1] = assumptionProfiles[index2];
            assumptionProfiles[index2] = temp;
        }

        public static void RemoveAssumptionProfile(int index)
        {
            assumptionProfiles.RemoveAt(index);
        }
    }
}
