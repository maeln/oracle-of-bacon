package com.serli.oracle.of.bacon.repository;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisRepository {
	private Jedis jedis;

	public RedisRepository() {
		jedis = new Jedis("localhost", 6379);
	}

	public void addSearch(String actorName) {
		jedis.rpush("searches", actorName);
	}

    public List<String> getLastTenSearches() {
        return jedis.lrange("searches", 0, 9);
    }
}
