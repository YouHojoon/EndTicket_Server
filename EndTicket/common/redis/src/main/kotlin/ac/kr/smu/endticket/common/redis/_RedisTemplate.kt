package ac.kr.smu.endticket.common.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions

/**
 * scan을 통해 패턴에 맞는 키를 가져오는 메소드
 * @param pattern 키의 패턴
 * @param count scan의 카운트, 기본값은 200
 * @return 조건에 맞는 키의 set
 */
fun RedisTemplate<String, Any>.getKeysWithPattern(
    pattern: String,
    count: Long = 200,
): Set<String> {
    val keys = HashSet<String>()

    execute {
        try {
            scan(
                ScanOptions
                    .scanOptions()
                    .match(pattern)
                    .count(count)
                    .build(),
            ).use {
                while (it.hasNext()) {
                    keys.add(it.next())
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    return keys
}
