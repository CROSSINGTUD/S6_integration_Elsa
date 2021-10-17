using System.Collections.Generic;
using System.IO;
using System.Text.Json;
using System.Threading.Tasks;

namespace ComposableCrypto.Core
{
    public class AssumptionProfile
    {
        private static readonly JsonSerializerOptions jsonOptions = new JsonSerializerOptions(JsonSerializerDefaults.Web);

        public string Name { get; set; }
        public IDictionary<string, bool> Assumptions { get; set; } = new Dictionary<string, bool>();

        public static ValueTask<AssumptionProfile> FromJson(Stream json)
        {
            return JsonSerializer.DeserializeAsync<AssumptionProfile>(json, jsonOptions);
        }

        public static AssumptionProfile FromJson(string json)
        {
            return JsonSerializer.Deserialize<AssumptionProfile>(json, jsonOptions);
        }
    }
}
