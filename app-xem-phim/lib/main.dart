import 'package:flutter/material.dart';
import 'screens/movie_list_screen.dart';
import 'screens/login_screen.dart';
void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Ứng dụng Xem Phim',
      theme: ThemeData(
        primarySwatch: Colors.orange,
      ),
      home: MovieListScreen(),
      debugShowCheckedModeBanner: false,// Màn hình danh sách phim
    );
  }
}
