import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Thêm thư viện này

class FilterMenu extends StatefulWidget {
  final Future<Map<String, dynamic>> Function() fetchFilterOptions;
  final Function(String?, String?, int?, int?, int?) onApplyFilter;
  final String? currentCategory;
  final String? currentSort;
  final int? currentGenre;
  final int? currentCountry;
  final int? currentYear;

  FilterMenu({
    required this.fetchFilterOptions,
    required this.onApplyFilter,
    this.currentCategory,
    this.currentSort,
    this.currentGenre,
    this.currentCountry,
    this.currentYear,
  });

  @override
  _FilterMenuState createState() => _FilterMenuState();
}

class _FilterMenuState extends State<FilterMenu> {
  String? selectedCategory;
  String? selectedSort;
  int? selectedGenre;
  int? selectedCountry;
  int? selectedYear;
  late Future<Map<String, dynamic>> filterOptionsFuture;

  @override
  void initState() {
    super.initState();
    selectedCategory = widget.currentCategory;
    selectedSort = widget.currentSort;
    selectedGenre = widget.currentGenre;
    selectedCountry = widget.currentCountry;
    selectedYear = widget.currentYear;

    filterOptionsFuture = widget.fetchFilterOptions();

    // Ẩn thanh trạng thái
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge, overlays: []);
  }

  @override
  void dispose() {
    // Khôi phục thanh trạng thái khi thoát
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge, overlays: SystemUiOverlay.values);
    super.dispose();
  }

  Widget buildFilterOption<T>({
    required String label,
    required List<T> options,
    required T? selectedOption,
    required String Function(T) getLabel,
    required Function(T?) onSelected,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
        ),
        SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: options.map((option) {
            final isSelected = option == selectedOption;
            return GestureDetector(
              onTap: () {
                onSelected(isSelected ? null : option); // Bỏ chọn nếu đã chọn
                setState(() {}); // Cập nhật UI
              },
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  color: isSelected ? Colors.pinkAccent : Colors.grey[800],
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  getLabel(option),
                  style: TextStyle(
                    color: isSelected ? Colors.white : Colors.grey[300],
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Map<String, dynamic>>(
      future: filterOptionsFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          return Center(child: Text('Lỗi: ${snapshot.error}', style: TextStyle(color: Colors.white)));
        } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
          return Center(child: Text('Không tìm thấy dữ liệu lọc.', style: TextStyle(color: Colors.white)));
        }

        final filterOptions = snapshot.data!;
        final genres = (filterOptions['genres'] as List).map((e) => e as Map<String, dynamic>).toList();
        final countries = (filterOptions['countries'] as List).map((e) => e as Map<String, dynamic>).toList();
        final years = (filterOptions['releaseYears'] as List).map((e) => e as int).toList();

        return Container(
          padding: EdgeInsets.all(16.0),
          color: Colors.black,
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                buildFilterOption<String>(
                  label: 'Danh mục',
                  options: ['Phim Lẻ', 'Phim Bộ'],
                  selectedOption: selectedCategory,
                  getLabel: (option) => option,
                  onSelected: (option) => selectedCategory = option,
                ),
                SizedBox(height: 16),
                buildFilterOption<Map<String, dynamic>>(
                  label: 'Thể loại',
                  options: genres,
                  selectedOption: genres.firstWhere(
                        (genre) => genre['genreId'] == selectedGenre,
                    orElse: () => {},
                  ),
                  getLabel: (option) => option['genreName'] ?? '',
                  onSelected: (option) => selectedGenre = option?['genreId'],
                ),
                SizedBox(height: 16),
                buildFilterOption<Map<String, dynamic>>(
                  label: 'Quốc gia',
                  options: countries,
                  selectedOption: countries.firstWhere(
                        (country) => country['countryId'] == selectedCountry,
                    orElse: () => {},
                  ),
                  getLabel: (option) => option['countryName'] ?? '',
                  onSelected: (option) => selectedCountry = option?['countryId'],
                ),
                SizedBox(height: 16),
                buildFilterOption<int>(
                  label: 'Năm phát hành',
                  options: years,
                  selectedOption: selectedYear,
                  getLabel: (option) => option.toString(),
                  onSelected: (option) => selectedYear = option,
                ),
                SizedBox(height: 20),
                ElevatedButton(
                  onPressed: () {
                    widget.onApplyFilter(
                      selectedCategory == 'Phim Lẻ' ? 'phimle' : 'phimbo', // Chuyển đổi danh mục
                      selectedSort,
                      selectedGenre,
                      selectedCountry,
                      selectedYear,
                    );
                  },
                  child: Text("Áp Dụng"),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
