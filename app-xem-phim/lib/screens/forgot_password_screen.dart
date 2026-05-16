// import 'package:flutter/material.dart';
// import '../services/auth_service.dart';
// class ForgotPasswordScreen extends StatelessWidget {
//   final _emailController = TextEditingController();
//   final _authService = AuthService();
//
//   Future<void> _forgotPassword() async {
//     try {
//       String message = await _authService.forgotPassword(_emailController.text);
//       ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
//     } catch (error) {
//       ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error.toString())));
//     }
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(title: Text('Quên Mật Khẩu')),
//       body: Padding(
//         padding: const EdgeInsets.all(16.0),
//         child: Column(
//           children: [
//             TextField(
//               controller: _emailController,
//               decoration: InputDecoration(labelText: 'Email'),
//             ),
//             SizedBox(height: 20),
//             ElevatedButton(
//               onPressed: _forgotPassword,
//               child: Text('Lấy lại mật khẩu'),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }
