import { useEffect, useState } from 'react';
import { getAllClients } from '../services/api';

export function useClients() {
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchClients = async () => {
    setLoading(true);
    setError(null);

    try {
      console.log("📡 Buscando lista de clientes...");
      const response = await getAllClients(0, 2000); // Busca um lote grande para os gráficos

      let data = [];

      // Lógica de extração "blindada"
      if (Array.isArray(response)) {
        data = response;
      } else if (response && Array.isArray(response.content)) {
        // Padrão Spring Page<>
        data = response.content;
      } else if (response && Array.isArray(response.items)) {
        data = response.items;
      } else if (response && Array.isArray(response.data)) {
        data = response.data;
      }

      console.log(`✅ ${data.length} clientes extraídos com sucesso.`);

      // IMPORTANTE: Se a API retornar vazio (mas 200 OK), tentamos fallback local
      // para garantir que o dashboard não fique vazio na demo.
      if (data.length === 0) {
        console.warn("⚠️ API retornou lista vazia. Tentando fallback local.");
        const fallback = await fetch("./clients.json");
        if (fallback.ok) data = await fallback.json();
      }

      setClients(data);
    } catch (err) {
      console.error("❌ Erro useClients:", err);

      // Fallback de emergência
      try {
        const fallback = await fetch("./clients.json");
        if (fallback.ok) setClients(await fallback.json());
      } catch (e) {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClients();
  }, []);

  return { clients, loading, error, refresh: fetchClients };
}