package com.hackdeck.adapter.rest.dto;

import java.util.List;

public record ReorderQueueRequest(List<String> intentIds) {
}
