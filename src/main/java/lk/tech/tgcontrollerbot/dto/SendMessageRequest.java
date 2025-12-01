package lk.tech.tgcontrollerbot.dto;

public record SendMessageRequest(Long chat_id, String text) {}