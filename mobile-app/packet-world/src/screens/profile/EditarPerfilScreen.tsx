import { Ionicons } from '@expo/vector-icons';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import * as ImagePicker from 'expo-image-picker';
import { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';

import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../../constants/theme';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import type { HomeStackParamList } from '../../navigation/types';

// URL base de la API
const API_BASE = 'https://packetworld.izekki.me//';

type Props = NativeStackScreenProps<HomeStackParamList, 'EditProfile'>;


export default function EditProfileScreen({ navigation }: Props) {
  const { user, updateUser } = useAuth();
  const { theme } = useTheme();
  const colors = theme.colors;

  const [loading, setLoading] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  // Mapeo de datos del usuario para visualización
  const userData = {
    numeroPersonal: user?.numeroPersonal || '',
    nombreCompleto: `${user?.nombre} ${user?.apellidoPaterno} ${user?.apellidoMaterno}`,
    rol: user?.rol || '',
    sucursal: user?.idCodigoSucursal || '',
    correo: user?.correoElectronico || '',
    curp: user?.curp || '',
    licencia: user?.numeroLicencia || '',
  };

  /**
   * Lanza la galería de imágenes del dispositivo
   */
  const pickImage = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
    });

    if (!result.canceled) {
      setSelectedImage(result.assets[0].uri);
    }
  };

  /**
   * Lanza la cámara del dispositivo tras verificar permisos
   */
  const takePhoto = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    
    if (status !== 'granted') {
      Alert.alert('Permiso denegado', 'Se requiere acceso a la cámara para tomar fotografías.');
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
    });

    if (!result.canceled) {
      setSelectedImage(result.assets[0].uri);
    }
  };

  /**
   * Muestra el diálogo de selección de origen para la fotografía
   */
  const handlePhotoPress = () => {
    Alert.alert(
      "Actualizar foto de perfil",
      "¿Cómo deseas obtener la nueva fotografía?",
      [
        { text: "Cámara", onPress: takePhoto },
        { text: "Galería", onPress: pickImage },
        { text: "Cancelar", style: "cancel" }
      ]
    );
  };

  /**
   * Realiza la carga de la imagen al servidor como un archivo binario
   */
  const handleSavePhoto = async () => {
    if (!selectedImage) return;

    setLoading(true);
    try {
      const endpoint = `${API_BASE}api/colaboradores/guardarFoto/${userData.numeroPersonal}`;
      
      // Obtenemos el blob binario a partir de la URI local de la imagen
      const responseUri = await fetch(selectedImage);
      const blob = await responseUri.blob();

      //console.log('Subiendo archivo binario a:', endpoint);

      const uploadResponse = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Content-Type': 'image/jpeg', // Indicamos que el cuerpo es un archivo de imagen
        },
        body: blob,
      });

      const textResponse = await uploadResponse.text();
      let data;
      
      try {
        data = JSON.parse(textResponse);
      } catch (e) {
        throw new Error('La respuesta del servidor no es un JSON válido.');
      }

      if (!data.error) {
        // Actualizamos el contexto global con la URI local para feedback inmediato
        updateUser({ fotografia: selectedImage });

        Alert.alert('Éxito', 'Tu fotografía de perfil ha sido actualizada.', [
          { text: 'Aceptar', onPress: () => navigation.goBack() }
        ]);
      } else {
        Alert.alert('Error', data.mensaje || 'No se pudo procesar la subida de la foto.');
      }

    } catch (error: any) {
      //console.error('Error al subir foto:', error);
      Alert.alert('Error de conexión', 'No se pudo conectar con el servidor. Revisa tu conexión a internet.');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Renderiza una fila de información
   */
  const InfoField = ({ label, value }: { label: string; value: string }) => (
    <View style={styles.infoRow}>
      <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>{label}</Text>
      <Text style={[styles.infoValue, { color: colors.text }]}>{value}</Text>
    </View>
  );

  return (
    <KeyboardAvoidingView 
      style={{ flex: 1 }} 
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView style={[styles.container, { backgroundColor: colors.background }]}>
        
        {/* Sección de cabecera con la fotografía circular interactiva */}
        <View style={styles.header}>
          <TouchableOpacity onPress={handlePhotoPress} activeOpacity={0.8}>
            <View style={[styles.avatarFrame, { borderColor: colors.primary, backgroundColor: colors.card }]}>
              <Image 
                source={{ uri: selectedImage || user?.fotografia }} 
                style={styles.profileImage} 
              />
              <View style={[styles.cameraBadge, { backgroundColor: colors.primary }]}>
                <Ionicons name="camera-outline" size={20} color="#FFF" />
              </View>
            </View>
          </TouchableOpacity>
          <Text style={[styles.instruction, { color: colors.textSecondary }]}>
            Toca para cambiar la fotografía
          </Text>
        </View>

        {/* Tarjeta de Datos Laborales (Solo lectura) */}
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.cardTitle, { color: colors.primary }]}>Datos de Empleado</Text>
          <InfoField label="ID Personal" value={userData.numeroPersonal} />
          <InfoField label="Cargo" value={userData.rol} />
          <InfoField label="Sucursal" value={userData.sucursal} />
        </View>

        {/* Tarjeta de Datos Personales (Solo lectura) */}
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.cardTitle, { color: colors.primary }]}>Información Personal</Text>
          <InfoField label="Nombre Completo" value={userData.nombreCompleto} />
          <InfoField label="CURP" value={userData.curp} />
          <InfoField label="No. de Licencia" value={userData.licencia} />
          <InfoField label="Correo Electrónico" value={userData.correo} />
        </View>

        {/* Botón flotante para guardar cambios (solo visible si se seleccionó una foto nueva) */}
        {selectedImage && (
          <TouchableOpacity 
            style={[styles.saveButton, { backgroundColor: colors.primary }]}
            onPress={handleSavePhoto}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#FFF" />
            ) : (
              <View style={styles.btnContent}>
                <Ionicons name="cloud-upload-outline" size={24} color="#FFF" />
                <Text style={styles.saveButtonText}>Guardar Nueva Foto</Text>
              </View>
            )}
          </TouchableOpacity>
        )}

        <View style={{ height: 60 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: SPACING.m,
  },
  header: {
    alignItems: 'center',
    marginVertical: SPACING.l,
  },
  avatarFrame: {
    width: 160,
    height: 160,
    borderRadius: 80,
    borderWidth: 4,
    justifyContent: 'center',
    alignItems: 'center',
    overflow: 'hidden',
    position: 'relative',
    elevation: 4,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.1,
    shadowRadius: 5,
  },
  profileImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  cameraBadge: {
    position: 'absolute',
    bottom: 5,
    right: 15,
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 3,
    borderColor: '#FFF',
  },
  instruction: {
    marginTop: SPACING.s,
    fontSize: FONT_SIZE.s,
    fontWeight: '600',
  },
  card: {
    padding: SPACING.m,
    borderRadius: BORDER_RADIUS.m,
    borderWidth: 1,
    marginBottom: SPACING.m,
  },
  cardTitle: {
    fontSize: FONT_SIZE.m,
    fontWeight: 'bold',
    marginBottom: SPACING.m,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  infoRow: {
    marginBottom: SPACING.m,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(0,0,0,0.02)',
    paddingBottom: 4,
  },
  infoLabel: {
    fontSize: FONT_SIZE.xs,
    fontWeight: '700',
    marginBottom: 2,
  },
  infoValue: {
    fontSize: FONT_SIZE.m,
  },
  saveButton: {
    height: 56,
    borderRadius: BORDER_RADIUS.m,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: SPACING.s,
    elevation: 2,
  },
  btnContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  saveButtonText: {
    color: '#FFF',
    fontSize: FONT_SIZE.l,
    fontWeight: 'bold',
  },
});