import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:geocoding/geocoding.dart';
import '../../core/constants/app_colors.dart';

class MapLocationSelector extends StatefulWidget {
  final LatLng? initialLocation;
  final String? initialAddress;
  final Function(LatLng, String) onLocationSelected;

  const MapLocationSelector({
    super.key,
    this.initialLocation,
    this.initialAddress,
    required this.onLocationSelected,
  });

  @override
  State<MapLocationSelector> createState() => _MapLocationSelectorState();
}

class _MapLocationSelectorState extends State<MapLocationSelector> {
  LatLng? _selectedPoint;
  String? _selectedAddress;
  bool _isGeocoding = false;
  
  // Default to Merida, Mexico if no initial location
  final LatLng _defaultLocation = const LatLng(20.9674, -89.5926);

  @override
  void initState() {
    super.initState();
    _selectedPoint = widget.initialLocation;
    _selectedAddress = widget.initialAddress;
  }

  Future<void> _getAddressFromLatLng(LatLng point) async {
    setState(() {
      _isGeocoding = true;
      _selectedPoint = point;
    });

    try {
      List<Placemark> placemarks = await placemarkFromCoordinates(
        point.latitude, 
        point.longitude
      );

      if (placemarks.isNotEmpty) {
        Placemark place = placemarks[0];
        _selectedAddress = "${place.street}, ${place.subLocality}, ${place.locality}";
      } else {
        _selectedAddress = "Ubicación seleccionada (${point.latitude.toStringAsFixed(4)}, ${point.longitude.toStringAsFixed(4)})";
      }
    } catch (e) {
      _selectedAddress = "Coordenadas: ${point.latitude.toStringAsFixed(4)}, ${point.longitude.toStringAsFixed(4)}";
    } finally {
      setState(() => _isGeocoding = false);
    }
  }

  void _showMapPicker() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.85,
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
        ),
        child: Column(
          children: [
            const SizedBox(height: 12),
            Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.grey[300], borderRadius: BorderRadius.circular(2))),
            Padding(
              padding: const EdgeInsets.all(24.0),
              child: Row(
                children: [
                   const Expanded(
                    child: Text('Selecciona Consultorio', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            Expanded(
              child: Stack(
                children: [
                  FlutterMap(
                    options: MapOptions(
                      initialCenter: _selectedPoint ?? _defaultLocation,
                      initialZoom: 15.0,
                      onTap: (_, point) => _getAddressFromLatLng(point),
                    ),
                    children: [
                      TileLayer(
                        urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                        userAgentPackageName: 'com.clinica.vitalis',
                      ),
                      if (_selectedPoint != null)
                        MarkerLayer(
                          markers: [
                            Marker(
                              point: _selectedPoint!,
                              width: 80,
                              height: 80,
                              child: const Icon(
                                Icons.location_on,
                                color: AppColors.danger,
                                size: 45,
                              ),
                            ),
                          ],
                        ),
                    ],
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white,
                boxShadow: [BoxShadow(color: Colors.black.withAlpha(10), blurRadius: 10, offset: const Offset(0, -5))],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Dirección seleccionada:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: AppColors.textSecondary)),
                  const SizedBox(height: 8),
                  Text(
                    _isGeocoding ? "Obteniendo dirección..." : (_selectedAddress ?? "Toca el mapa para seleccionar"),
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: _selectedPoint != null && !_isGeocoding
                      ? () {
                          widget.onLocationSelected(_selectedPoint!, _selectedAddress!);
                          Navigator.pop(context);
                          setState(() {});
                        }
                      : null,
                    child: const Text('Confirmar Ubicación'),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: _showMapPicker,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.primary.withAlpha(20)),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withAlpha(10),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            const CircleAvatar(
              backgroundColor: Color(0xFFE8F0FE),
              child: Icon(Icons.map_outlined, color: AppColors.primary),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Ubicación del Consultorio', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 4),
                  Text(
                    _selectedAddress ?? "Toca para seleccionar en el mapa",
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(color: _selectedAddress != null ? AppColors.textPrimary : AppColors.textSecondary),
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: AppColors.textSecondary),
          ],
        ),
      ),
    );
  }
}
