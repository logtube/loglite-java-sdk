package io.github.logtube.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TopicAwareTest {

    public Set<String> easySet(String... val) {
        HashSet<String> set = new HashSet<>();
        Collections.addAll(set, val);
        return set;
    }

    @Test
    public void isTopicEnabled() {
        TopicAware topicAware = new TopicAware();
        topicAware.setTopics(null);
        assertTrue(topicAware.isTopicEnabled("topic1"));
        assertTrue(topicAware.isTopicEnabled("topic2"));
        assertTrue(topicAware.isTopicEnabled("topic3"));
        topicAware.setTopics(easySet());
        assertFalse(topicAware.isTopicEnabled("topic1"));
        assertFalse(topicAware.isTopicEnabled("topic2"));
        assertFalse(topicAware.isTopicEnabled("topic3"));
        topicAware.setTopics(easySet("NONE", "topic1"));
        assertFalse(topicAware.isTopicEnabled("topic1"));
        assertFalse(topicAware.isTopicEnabled("topic2"));
        assertFalse(topicAware.isTopicEnabled("topic3"));
        topicAware.setTopics(easySet("NONE"));
        assertFalse(topicAware.isTopicEnabled("topic1"));
        assertFalse(topicAware.isTopicEnabled("topic2"));
        assertFalse(topicAware.isTopicEnabled("topic3"));
        topicAware.setTopics(easySet("ALL", "-topic1"));
        assertFalse(topicAware.isTopicEnabled("topic1"));
        assertTrue(topicAware.isTopicEnabled("topic2"));
        assertTrue(topicAware.isTopicEnabled("topic3"));
        topicAware.setTopics(easySet("topic1", "topic2"));
        assertTrue(topicAware.isTopicEnabled("topic1"));
        assertTrue(topicAware.isTopicEnabled("topic2"));
        assertFalse(topicAware.isTopicEnabled("topic3"));
    }
}