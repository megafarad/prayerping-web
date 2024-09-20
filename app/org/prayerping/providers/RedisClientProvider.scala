package org.prayerping.providers

import org.apache.pekko.actor.ActorSystem
import com.google.inject.{ Inject, Provider, Singleton }
import play.api.Configuration
import redis.RedisClient

@Singleton
class RedisClientProvider @Inject() (system: ActorSystem, config: Configuration) extends Provider[RedisClient] {
  private val redisConfig = config.get[Configuration]("redis")
  val client: RedisClient = RedisClient(
    host = redisConfig.get[String]("host"),
    port = redisConfig.get[Int]("port")
  )(system)

  override def get(): RedisClient = client
}
