/**
 * Client Search Hook
 *
 * Provides state management and API integration for paginated client search
 * with filtering, sorting, and pagination capabilities.
 *
 * @module hooks/useClientSearch
 * @version 1.0.0
 * @author ChurnInsight Team
 */

import { useState, useCallback, useEffect, useRef } from 'react';
import { searchClients, getFilterOptions, getHighRiskClients } from '../services/api';

/** Default filter state */
const DEFAULT_FILTERS = {
  page: 0,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'desc',
  status: null,
  minProbability: null,
  maxProbability: null,
  gender: null,
  minAge: null,
  maxAge: null,
  country: null,
  subscriptionType: null,
  deviceType: null,
  startDate: null,
  endDate: null,
  isHeavyUser: null,
  offlineListening: null,
  userId: null,
};

/**
 * Custom hook for managing client search state and operations.
 *
 * @returns {Object} Search state and control functions
 * @property {Object|null} data - Current search results
 * @property {boolean} loading - Loading state indicator
 * @property {string|null} error - Error message if request failed
 * @property {Object} filters - Current filter state
 * @property {Object|null} filterOptions - Available filter values
 * @property {Function} search - Execute search with optional filter overrides
 * @property {Function} updateFilter - Update single filter value
 * @property {Function} clearFilters - Reset all filters to defaults
 * @property {Function} updateSort - Change sort field and direction
 * @property {Function} searchHighRisk - Quick filter for high-risk clients
 * @property {Function} goToPage - Navigate to specific page
 * @property {Function} nextPage - Navigate to next page
 * @property {Function} previousPage - Navigate to previous page
 * @property {boolean} hasResults - Whether results exist
 * @property {boolean} isEmpty - Whether search returned no results
 * @property {number} totalPages - Total number of pages
 * @property {number} totalElements - Total number of records
 * @property {number} currentPage - Current page number
 *
 * @example
 * const { data, loading, search, updateFilter } = useClientSearch();
 *
 * // Execute search
 * search({ status: 'WILL_CHURN' });
 *
 * // Update single filter
 * updateFilter('gender', 'Male');
 */
export function useClientSearch() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filterOptions, setFilterOptions] = useState(null);
  const [filters, setFilters] = useState(DEFAULT_FILTERS);

  const abortControllerRef = useRef(null);

  useEffect(() => {
    const loadOptions = async () => {
      try {
        const options = await getFilterOptions();
        setFilterOptions(options);
      } catch (err) {
        console.error('Failed to load filter options:', err);
      }
    };
    loadOptions();
  }, []);

  const search = useCallback(async (newFilters = {}) => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    const mergedFilters = { ...filters, ...newFilters };
    setFilters(mergedFilters);
    setLoading(true);
    setError(null);

    try {
      const response = await searchClients(mergedFilters);
      setData(response);
      return response;
    } catch (err) {
      if (err.name !== 'AbortError') {
        setError(err.message);
        console.error('Search failed:', err);
      }
      throw err;
    } finally {
      setLoading(false);
    }
  }, [filters]);

  const goToPage = useCallback((page) => search({ page }), [search]);

  const nextPage = useCallback(() => {
    if (data && !data.last) {
      search({ page: filters.page + 1 });
    }
  }, [data, filters.page, search]);

  const previousPage = useCallback(() => {
    if (data && !data.first) {
      search({ page: filters.page - 1 });
    }
  }, [data, filters.page, search]);

  const updateFilter = useCallback((key, value) => {
    search({ [key]: value, page: 0 });
  }, [search]);

  const clearFilters = useCallback(() => {
    search(DEFAULT_FILTERS);
  }, [search]);

  const updateSort = useCallback((sortBy, sortDir = 'desc') => {
    search({ sortBy, sortDir, page: 0 });
  }, [search]);

  const searchHighRisk = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getHighRiskClients(0, 10);
      setData(response);
      setFilters(prev => ({
        ...prev,
        status: 'WILL_CHURN',
        minProbability: 0.7,
        page: 0
      }));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    data,
    loading,
    error,
    filters,
    filterOptions,
    search,
    updateFilter,
    clearFilters,
    updateSort,
    searchHighRisk,
    goToPage,
    nextPage,
    previousPage,
    hasResults: data?.content?.length > 0,
    isEmpty: data?.content?.length === 0 && !loading,
    totalPages: data?.totalPages || 0,
    totalElements: data?.totalElements || 0,
    currentPage: filters.page,
  };
}

