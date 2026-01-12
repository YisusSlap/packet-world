import { Ionicons } from '@expo/vector-icons';
import { useEffect, useState } from 'react';
import { Image, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../constants/theme';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import type { HomeScreenProps } from '../navigation/types';

/**
 * Pantalla de Inicio (Home) - Packet-World
 * Presenta la identidad corporativa, el perfil del conductor con su foto,
 * y la hora de acceso para validación del sistema.
 */
export default function HomeScreen({ navigation }: HomeScreenProps) {
  const { user } = useAuth();
  const { theme } = useTheme();
  const colors = theme.colors;

  // Estado para capturar el momento exacto del acceso
  const [accessTime, setAccessTime] = useState('');

  useEffect(() => {
    // Registramos la fecha y hora actual al montar el componente
    const now = new Date();
    const formattedTime = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const formattedDate = now.toLocaleDateString();
    setAccessTime(`${formattedDate} ${formattedTime}`);
  }, []);

  // Datos del conductor con valores de respaldo (fallback)
  const conductorName = user ? `${user.nombre} ${user.apellidoPaterno}` : 'Conductor';
  const conductorId = user?.numeroPersonal || 'N/A';
  const unitId = user?.idUnidadAsignada || 'Sin asignar';

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      
      {/* --- CABECERA CORPORATIVA --- */}
      <View style={styles.brandHeader}>
        <View style={styles.logoRow}>
          {/* Se utiliza .png para asegurar compatibilidad nativa y evitar 
            errores de resolución de módulos en entornos de desarrollo.
          */}
          <View style={[styles.logoContainer, { backgroundColor: colors.primary + '10' }]}>
            <Image 
              source={require('../../assets/icono_logo.png')} 
              style={styles.brandLogo}
              resizeMode="contain"
            />
          </View>
          <Text style={[styles.brandName, { color: colors.primary }]}>Packet-World</Text>
        </View>

        {/* Registro de Tiempo de Acceso */}
        <View style={[styles.timeBadge, { backgroundColor: colors.success + '15' }]}>
          <Ionicons name="time-outline" size={14} color={colors.success} />
          <Text style={[styles.timeText, { color: colors.success }]}>
            Acceso: {accessTime}
          </Text>
        </View>
      </View>

      {/* --- TARJETA DE PERFIL --- */}
      <TouchableOpacity 
        style={[styles.profileCard, { backgroundColor: colors.card, borderColor: colors.border }]}
        onPress={() => navigation.navigate('EditProfile')}
        activeOpacity={0.8}
      >
        <View style={[styles.avatarWrapper, { borderColor: colors.primary }]}>
           {user?.fotografia ? (
             <Image 
               source={{ uri: user.fotografia }} 
               style={styles.profileImage} 
             />
           ) : (
             <Ionicons name="person" size={60} color={colors.primary} />
           )}
           
           <View style={[styles.editIcon, { backgroundColor: colors.primary }]}>
             <Ionicons name="camera" size={12} color="#FFF" />
           </View>
        </View>

        <Text style={[styles.greeting, { color: colors.textSecondary }]}>Bienvenido al sistema,</Text>
        <Text style={[styles.driverName, { color: colors.text }]}>{conductorName}</Text>
        
        <View style={[styles.separator, { backgroundColor: colors.border }]} />

        <View style={styles.detailRow}>
          <Ionicons name="id-card-outline" size={18} color={colors.textSecondary} />
          <Text style={[styles.detailText, { color: colors.textSecondary }]}>
            ID Personal: <Text style={{ color: colors.text, fontWeight: '600' }}>{conductorId}</Text>
          </Text>
        </View>

        <View style={styles.detailRow}>
          <Ionicons name="car-outline" size={18} color={colors.textSecondary} />
          <Text style={[styles.detailText, { color: colors.textSecondary }]}>
            Unidad: <Text style={{ color: colors.text, fontWeight: '600' }}>{unitId}</Text>
          </Text>
        </View>
      </TouchableOpacity>

      {/* --- ACCIONES PRINCIPALES --- */}
      <View style={styles.actionsGroup}>
        <TouchableOpacity 
          style={[styles.mainButton, { backgroundColor: colors.primary }]}
          onPress={() => navigation.navigate('ShipmentsList')}
          activeOpacity={0.8}
        >
          <Ionicons name="navigate-circle-outline" size={26} color="#FFF" style={{ marginRight: 10 }} />
          <Text style={styles.mainButtonText}>Gestionar Envíos Asignados</Text>
        </TouchableOpacity>
        
        <Text style={[styles.versionLabel, { color: colors.textSecondary }]}>
          Sincronizado y listo para la ruta.
        </Text>
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: SPACING.l,
  },
  // Cabecera
  brandHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: SPACING.xl,
    marginBottom: SPACING.xl,
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: SPACING.s,
  },
  logoContainer: {
    width: 44,
    height: 44,
    borderRadius: BORDER_RADIUS.s,
    justifyContent: 'center',
    alignItems: 'center',
    overflow: 'hidden',
  },
  brandLogo: {
    width: 32,
    height: 32,
  },
  brandName: {
    fontSize: FONT_SIZE.xl,
    fontWeight: '900',
    letterSpacing: -0.5,
  },
  timeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: SPACING.s,
    paddingVertical: 4,
    borderRadius: BORDER_RADIUS.full,
    gap: 4,
  },
  timeText: {
    fontSize: 10,
    fontWeight: 'bold',
  },
  // Tarjeta de Perfil
  profileCard: {
    padding: SPACING.xl,
    borderRadius: BORDER_RADIUS.l,
    alignItems: 'center',
    borderWidth: 1,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
    elevation: 4,
    marginBottom: SPACING.xxl,
    marginTop: SPACING.xxl,
  },
  avatarWrapper: {
    width: 120,
    height: 120,
    borderRadius: 60,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.m,
    overflow: 'hidden',
    position: 'relative',
    borderWidth: 2,
  },
  profileImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  editIcon: {
    position: 'absolute',
    bottom: 5,
    right: 15,
    borderRadius: 10,
    padding: 5,
    borderWidth: 1.5,
    borderColor: '#FFF',
  },
  greeting: {
    fontSize: FONT_SIZE.s,
    marginBottom: 4,
  },
  driverName: {
    fontSize: FONT_SIZE.xl,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: SPACING.m,
  },
  separator: {
    height: 1,
    width: '100%',
    marginBottom: SPACING.m,
    opacity: 0.2,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: SPACING.s,
    width: '100%',
    justifyContent: 'center',
    gap: SPACING.s,
  },
  detailText: {
    fontSize: FONT_SIZE.m,
  },
  // Acciones
  actionsGroup: {
    width: '100%',
    alignItems: 'center',
    gap: SPACING.m,
  },
  mainButton: {
    flexDirection: 'row',
    width: '100%',
    height: 60,
    borderRadius: BORDER_RADIUS.m,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
  },
  mainButtonText: {
    color: '#FFFFFF',
    fontSize: FONT_SIZE.l,
    fontWeight: 'bold',
  },
  versionLabel: {
    fontSize: FONT_SIZE.xs,
    fontStyle: 'italic',
    opacity: 0.6,
  }
});