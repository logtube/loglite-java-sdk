package io.github.logtube.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.InvalidURIException;
import redis.clients.util.JedisURIHelper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * 创建此类目的：将内部的Client改为使用XLogRedisClient类
 *
 * @author chenkeguang 2018年10月19日
 */
public class LogtubeJedis extends Jedis {

    public LogtubeJedis() {
        this.client = new LogtubeRedisClient();
    }

    public LogtubeJedis(final String host) {
        URI uri = URI.create(host);
        if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
            initializeClientFromURI(uri);
        } else {
            client = new LogtubeRedisClient(host);
        }
    }

    public LogtubeJedis(final String host, final int port) {
        client = new LogtubeRedisClient(host, port);
    }

    public LogtubeJedis(final String host, final int port, final int connectionTimeout, final int soTimeout,
                        final boolean ssl, final SSLSocketFactory sslSocketFactory, final SSLParameters sslParameters,
                        final HostnameVerifier hostnameVerifier) {
        client = new LogtubeRedisClient(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        client.setConnectionTimeout(connectionTimeout);
        client.setSoTimeout(soTimeout);
    }

    private void initializeClientFromURI(URI uri) {
        if (!JedisURIHelper.isValid(uri)) {
            throw new InvalidURIException(
                    String.format("Cannot open Redis connection due invalid URI. %s", uri));
        }

        client = new LogtubeRedisClient(uri.getHost(), uri.getPort(), uri.getScheme().equals("rediss"));

        String password = JedisURIHelper.getPassword(uri);
        if (password != null) {
            client.auth(password);
            client.getStatusCodeReply();
        }

        int dbIndex = JedisURIHelper.getDBIndex(uri);
        if (dbIndex > 0) {
            client.select(dbIndex);
            client.getStatusCodeReply();
            client.setDb(dbIndex);
        }
    }

}
