package buckelieg.validation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class consisting of map-related predicates
 */
public final class Maps {

    private Maps() {
        throw new UnsupportedOperationException("No instances of Maps");
    }

    /**
     * Checks if this map contains provided key
     *
     * @param key value of tested key
     * @param <K> key type
     * @param <V> value type
     * @return a {@linkplain Predicate} instance
     * @see Map#containsKey(Object)
     */
    public static <K, V> Predicate<Map<K, V>> containsKey(K key) {
        return map -> map.containsKey(key);
    }

    /**
     * Checks if this map contains provided value
     *
     * @param value a value to check presence for
     * @param <K>   key type
     * @param <V>   value type
     * @return a {@linkplain Predicate} instance
     * @see Map#containsValue(Object)
     */
    public static <K, V> Predicate<Map<K, V>> containsValue(V value) {
        return map -> map.containsValue(value);
    }

    /**
     * Checks the map for emptiness
     *
     * @param <K> key type
     * @param <V> value type
     * @return a {@linkplain Predicate} instance
     * @see Map#isEmpty()
     */
    public static <K, V> Predicate<Map<K, V>> isEmpty() {
        return Map::isEmpty;
    }

    /**
     * Checks if this map has size of provided one
     *
     * @param <K> key type
     * @param <V> value type
     * @return a {@linkplain Predicate} instance
     * @see Map#size()
     */
    public static <K, V> Predicate<Map<K, V>> sizeOf(int size) {
        return map -> map.size() == size;
    }

    /**
     * Checks the value in the map under provided key against provided predicate
     *
     * @param key       a key the value is obtained by
     * @param predicate value test predicate
     * @param <K>       key type
     * @param <V>       value type
     * @return a {@linkplain Predicate} instance
     */
    public static <K, V> Predicate<Map<K, V>> keyValue(K key, Predicate<V> predicate) {
        return map -> predicate.test(map.get(key));
    }

    /**
     * Checks if the value in the map is equal to provided one by means of invoking {@linkplain Objects#equals(Object, Object)}
     *
     * @param key     a key the value is obtained by
     * @param measure measure value
     * @param <K>     key type
     * @param <V>     value type
     * @return a {@linkplain Predicate} instance
     * @see Objects#equals(Object, Object)
     */
    public static <K, V> Predicate<Map<K, V>> keyValueEquals(K key, V measure) {
        return map -> Objects.equals(measure, map.get(key));
    }

}
