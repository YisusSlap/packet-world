import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';

// Importaciones de contexto y constantes 
import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../../constants/theme';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

// URL base 
const API_BASE = 'https://packetworld.izekki.me//';

type ShipmentSummary = {
  numeroGuia: string;
  destinoCalle: string;
  estatusActual: string;
};


export default function ShipmentsListScreen({ navigation }: any) {
  const { user } = useAuth();
  const { theme } = useTheme();
  const colors = theme.colors;

  // Estados locales
  const [shipments, setShipments] = useState<ShipmentSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  /**
   * Obtiene lla lista de paquetes asignados al conductor
   */
  const fetchShipments = useCallback(async () => {
    if (!user?.numeroPersonal) return;

    try {
      const endpoint = `${API_BASE}api/envios/conductor/${user.numeroPersonal}`;
      
      const response = await fetch(endpoint, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        // Aseguramos que la respuesta sea un arreglo
        setShipments(Array.isArray(data) ? data : []);
      } else {
        //console.error('Error al obtener envíos. Estatus:', response.status);
      }
    } catch (error) {
      //console.error('Error de red en fetchShipments:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [user]);

  /**
   * Refresca la lista automáticamente al enfocar la pantalla. (Para cuando se regresa de la pantalla de detalles)
   */
  useFocusEffect(
    useCallback(() => {
      fetchShipments();
    }, [fetchShipments])
  );

  /**
   * Filtra la lista original basándose en numeroGuia o destinoCalle
   */
  const filteredShipments = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();
    if (!query) return shipments;

    return shipments.filter(item => 
      (item.numeroGuia && item.numeroGuia.toLowerCase().includes(query)) || 
      (item.destinoCalle && item.destinoCalle.toLowerCase().includes(query))
    );
  }, [searchQuery, shipments]);

  const onRefresh = () => {
    setRefreshing(true);
    fetchShipments();
  };

  /**
   * Mapeo de colores para el estatus visual
   */
  const getStatusColor = (status: string) => {
    const s = (status || '').toLowerCase();
    if (s.includes('entregado')) return colors.success;
    if (s.includes('cancelado') || s.includes('detenido')) return colors.error;
    if (s.includes('tránsito') || s.includes('procesado')) return colors.warning;
    return colors.textSecondary;
  };

  /**
   * Renderizado de cada tarjeta de envío
   */
  const renderItem = ({ item }: { item: ShipmentSummary }) => (
    <TouchableOpacity 
      style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}
      activeOpacity={0.7}
      onPress={() => {
        navigation.navigate('ShipmentDetail', { shipmentId: item.numeroGuia });
      }}
    >
      <View style={styles.cardHeader}>
        <View style={styles.guideContainer}>
          <Ionicons name="cube-outline" size={20} color={colors.primary} />
          <Text style={[styles.guideText, { color: colors.text }]}>
            {item.numeroGuia}
          </Text>
        </View>
        <View style={[
          styles.statusBadge, 
          { backgroundColor: getStatusColor(item.estatusActual) + '20' }
        ]}>
          <Text style={[styles.statusText, { color: getStatusColor(item.estatusActual) }]}>
            {(item.estatusActual || 'PENDIENTE').toUpperCase()}
          </Text>
        </View>
      </View>

      <View style={[styles.divider, { backgroundColor: colors.border, opacity: 0.2 }]} />

      <View style={styles.cardBody}>
        <View style={styles.row}>
          <Ionicons name="location-outline" size={18} color={colors.textSecondary} />
          <Text style={[styles.addressText, { color: colors.textSecondary }]} numberOfLines={2}>
            {item.destinoCalle || 'Dirección no especificada'}
          </Text>
        </View>
      </View>
      
      <View style={styles.cardFooter}>
        <Text style={[styles.detailLink, { color: colors.primary }]}>Gestionar Envío</Text>
        <Ionicons name="chevron-forward" size={16} color={colors.primary} />
      </View>
    </TouchableOpacity>
  );

  /**
   * Componente para cuando la lista está vacía o no hay resultados de búsqueda
   */
  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <Ionicons 
        name={searchQuery ? "search-outline" : "documents-outline"} 
        size={64} 
        color={colors.textSecondary} 
      />
      <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
        {searchQuery 
          ? `No se encontraron resultados para "${searchQuery}"`
          : "No tienes envíos asignados en este momento."}
      </Text>
      {!searchQuery && (
        <TouchableOpacity onPress={onRefresh} style={styles.refreshBtn}>
          <Text style={{ color: colors.primary, fontWeight: 'bold' }}>Sincronizar ahora</Text>
        </TouchableOpacity>
      )}
    </View>
  );

  if (loading && !refreshing) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={{ marginTop: 10, color: colors.textSecondary }}>Obteniendo paquetes...</Text>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView 
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={[styles.container, { backgroundColor: colors.background }]}
    >
      {/* Barra de Búsqueda */}
      <View style={[styles.searchContainer, { backgroundColor: colors.card, borderBottomColor: colors.border }]}>
        <View style={[styles.searchBar, { backgroundColor: colors.background, borderColor: colors.border }]}>
          <Ionicons name="search" size={20} color={colors.textSecondary} style={styles.searchIcon} />
          <TextInput
            style={[styles.searchInput, { color: colors.text }]}
            placeholder="Filtrar por guía o calle..."
            placeholderTextColor={colors.textSecondary}
            value={searchQuery}
            onChangeText={setSearchQuery}
            autoCapitalize="none"
            autoCorrect={false}
          />
          {searchQuery.length > 0 && (
            <TouchableOpacity onPress={() => setSearchQuery('')} style={styles.clearBtn}>
              <Ionicons name="close-circle" size={20} color={colors.textSecondary} />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Lista de Envios */}
      <FlatList
        data={filteredShipments}
        renderItem={renderItem}
        keyExtractor={(item) => item.numeroGuia}
        contentContainerStyle={filteredShipments.length === 0 ? styles.listEmpty : styles.listContent}
        ListEmptyComponent={renderEmpty}
        refreshControl={
          <RefreshControl 
            refreshing={refreshing} 
            onRefresh={onRefresh} 
            tintColor={colors.primary}
            colors={[colors.primary]}
          />
        }
      />
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  // Estilos de la Barra de Búsqueda
  searchContainer: {
    padding: SPACING.m,
    borderBottomWidth: 1,
    zIndex: 10,
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 46,
    borderRadius: BORDER_RADIUS.m,
    borderWidth: 1,
    paddingHorizontal: SPACING.s,
  },
  searchIcon: {
    marginRight: SPACING.s,
  },
  searchInput: {
    flex: 1,
    fontSize: FONT_SIZE.s,
    height: '100%',
  },
  clearBtn: {
    padding: SPACING.xs,
  },
  // Estilos de la Lista
  listContent: { padding: SPACING.m },
  listEmpty: { flexGrow: 1, justifyContent: 'center', alignItems: 'center', padding: SPACING.m },
  card: {
    borderRadius: BORDER_RADIUS.m,
    borderWidth: 1,
    marginBottom: SPACING.m,
    padding: SPACING.m,
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  cardHeader: { 
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    alignItems: 'center', 
    marginBottom: SPACING.s 
  },
  guideContainer: { flexDirection: 'row', alignItems: 'center', gap: SPACING.s },
  guideText: { fontSize: FONT_SIZE.m, fontWeight: 'bold' },
  statusBadge: { paddingHorizontal: SPACING.s, paddingVertical: 4, borderRadius: BORDER_RADIUS.s },
  statusText: { fontSize: FONT_SIZE.xs, fontWeight: 'bold' },
  divider: { height: 1, width: '100%', marginVertical: SPACING.s },
  cardBody: { marginBottom: SPACING.s },
  row: { flexDirection: 'row', gap: SPACING.s, alignItems: 'flex-start' },
  addressText: { fontSize: FONT_SIZE.s, flex: 1 },
  cardFooter: { 
    flexDirection: 'row', 
    justifyContent: 'flex-end', 
    alignItems: 'center', 
    marginTop: SPACING.s 
  },
  detailLink: { fontSize: FONT_SIZE.xs, fontWeight: '600', marginRight: 4 },
  emptyContainer: { alignItems: 'center', justifyContent: 'center', paddingBottom: 100 },
  emptyText: { fontSize: FONT_SIZE.m, textAlign: 'center', marginTop: SPACING.m },
  refreshBtn: { marginTop: 20, padding: 10 },
});