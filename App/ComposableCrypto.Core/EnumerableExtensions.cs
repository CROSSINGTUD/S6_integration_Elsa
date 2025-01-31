﻿using System.Collections.Generic;
using System.Linq;

namespace ComposableCrypto.Core
{
    public static class EnumerableExtensions
    {
        public static IEnumerable<(T item, int index)> WithIndex<T>(this IEnumerable<T> source) => source.Select((item, index) => (item, index));
    }
}
