import React, { createContext, useContext, useState } from 'react';
import { Alert } from 'react-native';

// --- CONFIGURACIÓN DE URL ---
const IP_Backend = 'https://packetworld.izekki.me//';

// Estructura del colaborador
export type UserData = {
  apellidoMaterno: string;
  apellidoPaterno: string;
  correoElectronico: string;
  curp: string;
  estatus: string;
  idCodigoSucursal: string;
  idUnidadAsignada: string;
  nombre: string;
  numeroLicencia: string;
  numeroPersonal: string;
  rol: string;
  fotografia?: string;
  contrasenia?: string; 
};

// Estructura del catálogo de estatus
export type StatusCatalogItem = {
  id: number;
  nombre: string;
};

type AuthContextType = {
  isAuthenticated: boolean;
  user: UserData | null;
  token: string | null;
  isLoading: boolean;
  shipmentStatuses: StatusCatalogItem[]; // Almacenamos el objeto completo (ID y Nombre)
  login: (numeroPersonal: string, contrasenia: string) => Promise<void>;
  logout: () => void;
  updateUser: (newData: Partial<UserData>) => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de un AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<UserData | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [shipmentStatuses, setShipmentStatuses] = useState<StatusCatalogItem[]>([]);

  // Funcion para obtener los catalogos y guardarlos
  const fetchStatusCatalog = async () => {
    try {
      const response = await fetch(`${IP_Backend}api/catalogos/estatusEnvio`);
      const data = await response.json();
      
      if (Array.isArray(data)) {
        setShipmentStatuses(data);
      } else {
        // Por si llega vacio
        setShipmentStatuses([
          { id: 3, nombre: 'en tránsito' },
          { id: 5, nombre: 'entregado' },
          { id: 4, nombre: 'detenido' },
          { id: 6, nombre: 'cancelado' }
        ]);
      }
    } catch (error) {
      //pOR SI HAY ERROR en la peticion
      //console.error('Error al cargar catálogo:', error);
      setShipmentStatuses([
        { id: 3, nombre: 'en tránsito' },
        { id: 5, nombre: 'entregado' },
        { id: 4, nombre: 'detenido' },
        { id: 6, nombre: 'cancelado' }
      ]);
    }
  };

  const login = async (numeroPersonal: string, contrasenia: string) => {
    setIsLoading(true);
    try {
      const API_URL = `${IP_Backend}api/autenticacion/movil`;
      
      const params = new URLSearchParams();
      params.append('numeroPersonal', numeroPersonal);
      params.append('contrasenia', contrasenia);

      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params.toString(),
      });

      const data = await response.json();

      if (!data.error && data.token) {
        setToken(data.token);
        
        let userData = data.colaborador;
        
        // Formateo de imagen base64
        if (userData.fotografia && !userData.fotografia.startsWith('data:image')) {
          userData.fotografia = `data:image/jpeg;base64,${userData.fotografia}`;
        }

        // Recuperar catálogos antes de autorizar el acceso
        await fetchStatusCatalog();

        setUser(userData);
        setIsAuthenticated(true);
      } else {
        Alert.alert('Error', data.mensaje || 'Credenciales incorrectas');
      }

    } catch (error) {
      Alert.alert('Error de conexión', 'Verifica tu conexión a internet.');
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    setShipmentStatuses([]);
    setIsAuthenticated(false);
  };

  const updateUser = (newData: Partial<UserData>) => {
    setUser((prevUser) => {
      if (!prevUser) return null;
      return { ...prevUser, ...newData };
    });
  };

  return (
    <AuthContext.Provider value={{ 
      isAuthenticated, 
      user, 
      token, 
      isLoading, 
      shipmentStatuses,
      login, 
      logout, 
      updateUser 
    }}>
      {children}
    </AuthContext.Provider>
  );
};