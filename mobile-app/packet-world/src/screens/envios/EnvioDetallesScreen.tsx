import { Ionicons } from '@expo/vector-icons';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Modal,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';

// --- MOCKS Y CONSTANTES (Para resolver errores de compilación local) ---
// Estos valores se definen aquí para asegurar que el archivo funcione independientemente
const SPACING = { xs: 4, s: 8, m: 16, l: 24, xl: 32 };
const FONT_SIZE = { xs: 12, s: 14, m: 16, l: 18, xl: 20 };
const BORDER_RADIUS = { s: 4, m: 8, l: 12, full: 99 };

// Importaciones simuladas de contextos si no se resuelven
import { StatusCatalogItem, useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import type { HomeStackParamList } from '../../navigation/types';

// Configuración de la API
const API_BASE = 'https://packetworld.izekki.me//';

type Props = NativeStackScreenProps<HomeStackParamList, 'ShipmentDetail'>;

type HistorialItem = {
  idHistorial: number;
  nombreEstatus: string;
  fechaCambio: string;
  comentario: string;
  numeroPersonalColaborador: string;
};

type PaqueteItem = {
  idPaquete: number;
  descripcion: string;
  pesoKg: number;
  dimAltoCm: number;
  dimAnchoCm: number;
  dimProfundidadCm: number;
};

type ShipmentDetail = {
  numeroGuia: string;
  estatusActual: string;
  destinoCalle: string;
  destinoNumero: string;
  nombreColonia: string;
  codigoPostal: string;
  ciudad: string;
  estado: string;
  destinatarioNombre: string;
  destinatarioAp1: string;
  destinatarioAp2: string;
  nombreCliente: string;
  codigoSucursalOrigen: string;
  costoTotal: number;
  historial: HistorialItem[];
  listaPaquetes: PaqueteItem[];
};

export default function ShipmentDetailScreen({ route, navigation }: Props) {
  const { shipmentId } = route.params;
  const { user, shipmentStatuses } = useAuth(); 
  const { theme } = useTheme();
  const colors = theme.colors;

  const [detail, setDetail] = useState<ShipmentDetail | null>(null);
  const [loading, setLoading] = useState(true);
  
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<StatusCatalogItem | null>(null);
  const [comment, setComment] = useState('');
  const [updating, setUpdating] = useState(false);

  const getStatusColor = (status: string) => {
    const s = (status || '').toLowerCase();
    if (s.includes('entregado')) return colors.success;
    if (s.includes('cancelado') || s.includes('detenido')) return colors.error;
    if (s.includes('tránsito') || s.includes('procesado') || s.includes('recibido')) return colors.warning;
    return colors.textSecondary;
  };

  const fetchDetail = useCallback(async () => {
    try {
      const endpoint = `${API_BASE}api/envios/rastrear/${shipmentId}`;
      const response = await fetch(endpoint);
      const textResponse = await response.text();
      
      try {
        const data = JSON.parse(textResponse);
        if (response.ok) {
          setDetail(data);
        } else {
          Alert.alert('Error', 'No se pudo cargar la información del envío.');
          navigation.goBack();
        }
      } catch (e) {
        console.error('Error parseando JSON de detalle:', textResponse);
        Alert.alert('Error del Servidor', 'Respuesta no válida del servidor.');
        navigation.goBack();
      }
    } catch (error) {
      Alert.alert('Error de Conexión', 'No se pudo establecer comunicación con el servidor.');
      navigation.goBack();
    } finally {
      setLoading(false);
    }
  }, [shipmentId, navigation]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  const handleUpdateStatus = async () => {
    if (!selectedStatus) {
      Alert.alert('Atención', 'Debes seleccionar un nuevo estado para continuar.');
      return;
    }

    const statusNameLower = (selectedStatus.nombre || '').toLowerCase();
    const isProblematic = statusNameLower.includes('detenido') || statusNameLower.includes('cancelado');
    
    if (isProblematic && !comment.trim()) {
      Alert.alert('Comentario requerido', 'Por favor, describe el motivo para este estado.');
      return;
    }

    setUpdating(true);
    try {
      const endpoint = `${API_BASE}api/envios/actualizarEstatus`;
      
      const details: Record<string, string> = {
        'numeroGuia': shipmentId,
        'estatus': selectedStatus.id.toString(), 
        'comentario': comment || 'Actualización desde App Móvil',
        'idConductor': user?.numeroPersonal || '',
      };

      const formBody = Object.keys(details)
        .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(details[key]))
        .join('&');

      const response = await fetch(endpoint, {
        method: 'PUT', 
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: formBody,
      });

      const textResponse = await response.text();
      let data;
      try {
        data = JSON.parse(textResponse);
      } catch (jsonError) {
        if (response.ok) {
           Alert.alert('Éxito', 'Estatus actualizado correctamente.');
           setModalVisible(false);
           setComment('');
           setSelectedStatus(null);
           setLoading(true); 
           fetchDetail(); 
           return;
        }
        throw new Error('El servidor devolvió una respuesta no válida (no JSON).');
      }

      if (!data.error) {
        Alert.alert('Éxito', data.mensaje || 'Estatus actualizado correctamente.');
        setModalVisible(false);
        setComment('');
        setSelectedStatus(null);
        setLoading(true); 
        fetchDetail(); 
      } else {
        Alert.alert('Atención', data.mensaje || 'El servidor reportó un error al procesar el cambio.');
      }

    } catch (error: any) {
      console.error('Error en updateStatus:', error);
      Alert.alert('Fallo de Conexión', `No pudimos contactar con el servidor. Detalle: ${error.message}`);
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={{ marginTop: 10, color: colors.textSecondary }}>Cargando información del envio...</Text>
      </View>
    );
  }

  if (!detail) return null;

  return (
    <View style={{ flex: 1 }}> 
      <ScrollView style={[styles.container, { backgroundColor: colors.background }]}>
        
        {/* --- ENCABEZADO: GUÍA Y ESTADO --- */}
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.headerRow}>
            <View>
              <Text style={[styles.label, { color: colors.textSecondary }]}>GUÍA DE RASTREO</Text>
              <Text style={[styles.guideText, { color: colors.primary }]}>{detail.numeroGuia}</Text>
            </View>
            <View style={[
              styles.statusBadge, 
              { backgroundColor: getStatusColor(detail.estatusActual) + '20' }
            ]}>
              <Text style={[styles.statusText, { color: getStatusColor(detail.estatusActual) }]}>
                {(detail.estatusActual || '').toUpperCase()}
              </Text>
            </View>
          </View>
        </View>

        {/* --- DATOS DEL DESTINATARIO --- */}
        <View style={styles.sectionTitleContainer}>
          <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>DESTINATARIO</Text>
        </View>
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.infoValue, { color: colors.text }]}>
            {detail.destinatarioNombre} {detail.destinatarioAp1} {detail.destinatarioAp2}
          </Text>
          <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>
            {detail.destinoCalle} #{detail.destinoNumero}, {detail.nombreColonia}
          </Text>
          <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>
            {detail.ciudad}, {detail.estado} (CP: {detail.codigoPostal})
          </Text>
        </View>

        {/* --- LISTA DE PAQUETES --- */}
        <View style={styles.sectionTitleContainer}>
          <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
            CONTENIDO DEL ENVÍO ({detail.listaPaquetes?.length || 0} PAQUETES)
          </Text>
        </View>
        {detail.listaPaquetes?.map((pkg, i) => (
          <View key={pkg.idPaquete} style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.packageHeader}>
               <Ionicons name="cube-outline" size={20} color={colors.primary} />
               <Text style={[styles.packageTitle, { color: colors.text }]}>Paquete #{i+1}</Text>
            </View>
            <Text style={[styles.packageDesc, { color: colors.text }]}>{pkg.descripcion}</Text>
            <Text style={[styles.packageMeta, { color: colors.textSecondary }]}>
              Medidas: {pkg.dimAltoCm}x{pkg.dimAnchoCm}x{pkg.dimProfundidadCm} cm
            </Text>
            <Text style={[styles.packageMeta, { color: colors.textSecondary }]}>
              Peso: {pkg.pesoKg} kg
            </Text>
          </View>
        ))}

        {/* --- HISTORIAL DE EVENTOS --- */}
        <View style={styles.sectionTitleContainer}>
          <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>HISTORIAL DE EVENTOS</Text>
        </View>
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          {detail.historial?.map((event, index) => (
            <View key={event.idHistorial} style={styles.historyItem}>
              <View style={styles.timelineContainer}>
                <View style={[styles.timelineDot, { backgroundColor: index === 0 ? colors.primary : colors.border }]} />
                {(detail.historial && index < detail.historial.length - 1) && (
                  <View style={[styles.timelineLine, { backgroundColor: colors.border }]} />
                )}
              </View>
              <View style={styles.historyContent}>
                <Text style={[styles.historyStatus, { color: colors.text }]}>{(event.nombreEstatus || '').toUpperCase()}</Text>
                <Text style={[styles.historyDate, { color: colors.textSecondary }]}>{event.fechaCambio}</Text>
                <Text style={[styles.historyComment, { color: colors.textSecondary }]}>{event.comentario}</Text>
              </View>
            </View>
          ))}
        </View>

        <View style={{ height: 120 }} />
      </ScrollView>

      {/* --- BOTÓN DE ACCIÓN FLOTANTE --- */}
      <View style={[styles.fabContainer, { backgroundColor: colors.card, borderTopColor: colors.border }]}>
        <TouchableOpacity 
          style={[styles.updateButton, { backgroundColor: colors.primary }]}
          onPress={() => setModalVisible(true)}
        >
          <Ionicons name="sync-circle-outline" size={24} color="#FFF" style={{ marginRight: 8 }} />
          <Text style={styles.updateButtonText}>Actualizar Estatus</Text>
        </TouchableOpacity>
      </View>

      {/* --- MODAL DE GESTIÓN --- */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <KeyboardAvoidingView 
          behavior={Platform.OS === "ios" ? "padding" : "height"}
          style={styles.modalOverlay}
        >
          <View style={[styles.modalContent, { backgroundColor: colors.card }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>Gestión del Envío</Text>
            
            <Text style={[styles.label, { color: colors.textSecondary, marginTop: 10 }]}>Selecciona el nuevo estado:</Text>
            <View style={styles.statusOptionsContainer}>
              {/* FILTRADO ROBUSTO: Se asegura de que ID 1 y 2 sean excluidos sin importar el tipo (string o number) */}
              {shipmentStatuses
                .filter(statusObj => {
                   const id = String(statusObj.id).trim();
                   return id !== '1' && id !== '2';
                })
                .map(statusObj => (
                <TouchableOpacity
                  key={statusObj.id}
                  style={[
                    styles.statusOption,
                    { 
                      borderColor: colors.border,
                      backgroundColor: selectedStatus?.id === statusObj.id ? colors.primary : 'transparent' 
                    }
                  ]}
                  onPress={() => setSelectedStatus(statusObj)}
                >
                  <Text style={{ 
                    color: selectedStatus?.id === statusObj.id ? '#FFF' : colors.text,
                    fontWeight: selectedStatus?.id === statusObj.id ? 'bold' : 'normal'
                  }}>
                    {statusObj.nombre}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[styles.label, { color: colors.textSecondary, marginTop: 15 }]}>
              Comentario {selectedStatus?.nombre.toLowerCase().match(/detenido|cancelado/) ? '(Obligatorio)' : '(Opcional)'}:
            </Text>
            <TextInput
              style={[styles.input, { 
                backgroundColor: colors.background, 
                color: colors.text, 
                borderColor: colors.border 
              }]}
              placeholder="Detalle el motivo..."
              placeholderTextColor={colors.textSecondary}
              value={comment}
              onChangeText={setComment}
              multiline
            />

            <View style={styles.modalButtons}>
              <TouchableOpacity 
                style={[styles.modalButton, { backgroundColor: colors.error }]}
                onPress={() => {
                  setModalVisible(false);
                  setComment('');
                  setSelectedStatus(null);
                }}
              >
                <Text style={styles.modalButtonText}>Cancelar</Text>
              </TouchableOpacity>

              <TouchableOpacity 
                style={[styles.modalButton, { backgroundColor: colors.success }]}
                onPress={handleUpdateStatus}
                disabled={updating}
              >
                {updating ? (
                  <ActivityIndicator color="#FFF" />
                ) : (
                  <Text style={styles.modalButtonText}>Confirmar</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>

    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: SPACING.m },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  card: { borderRadius: BORDER_RADIUS.m, borderWidth: 1, padding: SPACING.m, marginBottom: SPACING.m },
  headerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  label: { fontSize: FONT_SIZE.xs, fontWeight: 'bold', marginBottom: 4 },
  guideText: { fontSize: FONT_SIZE.xl, fontWeight: 'bold' },
  statusBadge: { paddingHorizontal: SPACING.s, paddingVertical: 4, borderRadius: BORDER_RADIUS.s },
  statusText: { fontSize: FONT_SIZE.xs, fontWeight: 'bold' },
  sectionTitleContainer: { marginBottom: SPACING.s, marginTop: SPACING.s, paddingHorizontal: SPACING.xs },
  sectionTitle: { fontSize: FONT_SIZE.xs, fontWeight: 'bold', letterSpacing: 1 },
  infoValue: { fontSize: FONT_SIZE.m, fontWeight: '500', marginBottom: 2 },
  infoLabel: { fontSize: FONT_SIZE.s, marginBottom: 2 },
  packageHeader: { flexDirection: 'row', alignItems: 'center', gap: SPACING.s, marginBottom: SPACING.xs },
  packageTitle: { fontWeight: 'bold', fontSize: FONT_SIZE.m },
  packageDesc: { fontSize: FONT_SIZE.m, marginBottom: SPACING.xs },
  packageMeta: { fontSize: FONT_SIZE.s },
  historyItem: { flexDirection: 'row', marginBottom: SPACING.l },
  timelineContainer: { alignItems: 'center', width: 20, marginRight: SPACING.s },
  timelineDot: { width: 12, height: 12, borderRadius: 6, zIndex: 1 },
  timelineLine: { width: 2, flex: 1, marginTop: -2 },
  historyContent: { flex: 1, marginTop: -4 },
  historyStatus: { fontWeight: 'bold', fontSize: FONT_SIZE.m },
  historyDate: { fontSize: FONT_SIZE.xs, marginBottom: 4 },
  historyComment: { fontSize: FONT_SIZE.s, fontStyle: 'italic' },
  fabContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: SPACING.m,
    borderTopWidth: 1,
    elevation: 8,
  },
  updateButton: {
    flexDirection: 'row',
    height: 56,
    borderRadius: BORDER_RADIUS.m,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 4,
  },
  updateButtonText: { color: '#FFF', fontSize: FONT_SIZE.l, fontWeight: 'bold' },
  modalOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.6)',
  },
  modalContent: {
    width: '90%',
    padding: SPACING.l,
    borderRadius: BORDER_RADIUS.l,
    elevation: 10,
  },
  modalTitle: { fontSize: FONT_SIZE.xl, fontWeight: 'bold', textAlign: 'center', marginBottom: SPACING.m },
  statusOptionsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: SPACING.s,
    marginTop: SPACING.s,
    justifyContent: 'center',
  },
  statusOption: {
    paddingHorizontal: SPACING.m,
    paddingVertical: SPACING.s,
    borderRadius: BORDER_RADIUS.full,
    borderWidth: 1,
    minWidth: '45%',
    alignItems: 'center',
  },
  input: {
    borderWidth: 1,
    borderRadius: BORDER_RADIUS.m,
    padding: SPACING.m,
    marginTop: SPACING.s,
    height: 100,
    textAlignVertical: 'top',
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: SPACING.xl,
    gap: SPACING.m,
  },
  modalButton: {
    flex: 1,
    padding: SPACING.m,
    borderRadius: BORDER_RADIUS.m,
    alignItems: 'center',
  },
  modalButtonText: { color: '#FFF', fontWeight: 'bold' },
});