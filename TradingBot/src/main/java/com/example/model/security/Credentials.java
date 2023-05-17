package com.example.model.security;

import com.example.model.order.Client;

public record Credentials(Client client, String password) {
}
