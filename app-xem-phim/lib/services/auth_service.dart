import 'dart:convert';
import 'package:http/http.dart' as http;

class AuthService {
  final String baseUrl = 'http://localhost:8080/api/users'; // URL API

  Future<String> register(Map<String, dynamic> user) async {
    final response = await http.post(
      Uri.parse('$baseUrl/register'),
      body: jsonEncode(user),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      return response.body;
    } else {
      throw Exception('Error: ${response.body}');
    }
  }

  Future<String> login(String username, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      body: {'username': username, 'password': password},
    );

    if (response.statusCode == 200) {
      return response.body;
    } else {
      throw Exception('Error: ${response.body}');
    }
  }

  Future<String> forgotPassword(String email) async {
    final response = await http.post(
      Uri.parse('$baseUrl/forgot-password'),
      body: {'email': email},
    );

    if (response.statusCode == 200) {
      return response.body;
    } else {
      throw Exception('Error: ${response.body}');
    }
  }
}
