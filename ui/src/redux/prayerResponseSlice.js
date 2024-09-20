import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';

export const fetchPrayerResponses = createAsyncThunk(
  'api/fetchPrayerResponses',
  async ({ prayerId, page }) => {
    const response = await fetch(`/api/prayers/${prayerId}/responses?page=${page}`);
    const data = await response.json();
    const lastPage = data.items.length + data.offset === data.total;
    return { responses: data.items, prayerId, page, hasMore: !lastPage };
  }
);

export const respondToPrayer = createAsyncThunk(
  'api/respondToPrayer',
  async ({prayerId, form}) => {
    const response = await fetch(`/api/prayers/${prayerId}/responses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
      body: JSON.stringify(form)
    });
    return await response.json();
  }
);

export const updatePrayerResponse = createAsyncThunk(
  'api/updatePrayerResponse',
  async ({responseId, form}) => {
    const fetchResponse = await fetch(`/api/responses/${responseId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
      body: JSON.stringify(form)
    });
    return await fetchResponse.json();
  }
)

export const deletePrayerResponse = createAsyncThunk(
  'api/deletePrayerResponse',
  async ({responseId}) => {
    await fetch(`/api/responses/${responseId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include'
    });
    return { responseId };
  }
);

const prayerResponseSlice = createSlice({
  name: 'prayerResponses',
  initialState: {
    responses: {},
    loading: {},
    error: {},
    hasMore: {},
  },
  reducers: {
    clearResponses: (state, action) => {
      const { prayerId } = action.payload;
      if (prayerId) {
        delete state.responses[prayerId];
        delete state.loading[prayerId];
        delete state.error[prayerId];
        delete state.hasMore[prayerId];
      } else {
        state.responses = {};
        state.loading = {};
        state.error = {};
        state.hasMore = {};
      }
    },
    addPrayerResponseReducer: (state, action) => {
      const { id, requestId } = action.payload;
      const existingResponses = state.responses[requestId];
      const hasMore = state.hasMore[requestId];
      const existingIdx = existingResponses.findIndex(response => response.id === id);
      if (existingIdx === -1 && !hasMore) {
        state.responses[requestId].push(action.payload);
      }
    },
    updatePrayerResponseReducer: (state, action) => {
      const { id, requestId } = action.payload;
      const existingResponses = state.responses[requestId];
      const existingIdx = existingResponses.findIndex(response => response.id === id);
      if (existingIdx !== -1) {
        state.responses[requestId][existingIdx] = action.payload;
      }
    },
    removePrayerResponseReducer: (state, action) => {
      const { id, requestId } = action.payload;
      state.responses[requestId] = state.responses[requestId].filter(response => response.id !== id);
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPrayerResponses.pending, (state, action) => {
        const { prayerId } = action.meta.arg;
        state.loading[prayerId] = true;
        state.error[prayerId] = null;
      })
      .addCase(fetchPrayerResponses.fulfilled, (state, action) => {
        const { responses, prayerId, page, hasMore } = action.payload;
        state.loading[prayerId] = false;
        state.responses[prayerId] = state.responses[prayerId] || [];
        if (page === 0) {
          state.responses[prayerId] = responses;
        } else {
          state.responses[prayerId] = [...state.responses[prayerId], ...responses];
        }
        state.hasMore[prayerId] = hasMore;
      })
      .addCase(fetchPrayerResponses.rejected, (state, action) => {
        const { prayerId } = action.meta.arg;
        state.loading[prayerId] = false;
        state.error[prayerId] = action.error.message;
      })
      .addCase(respondToPrayer.fulfilled, (state, action) => {
        const responsePayload = action.payload;
        if (!state.responses[responsePayload.requestId]) {
          state.responses[responsePayload.requestId] = [];
        }
        const hasMore = state.hasMore[responsePayload.requestId];
        const existingIdx = state.responses[responsePayload.requestId].findIndex(response => responsePayload.id === response.id);
        if (existingIdx === -1 && !hasMore) {
          state.responses[responsePayload.requestId].push(responsePayload);
        }
      })
      .addCase(updatePrayerResponse.fulfilled, (state, action) => {
        const updatedResponse = action.payload;
        const { requestId } = updatedResponse;
        if (state.responses[requestId]) {
          state.responses[requestId] = state.responses[requestId].map(response =>
            response.id === updatedResponse.id ? updatedResponse : response
          );
        }
      })
      .addCase(deletePrayerResponse.fulfilled, (state, action) => {
        const { responseId } = action.payload;
        for (let prayerId in state.responses) {
          state.responses[prayerId] = state.responses[prayerId].filter(response => response.id !== responseId);
        }
      });
  },
});

export const { clearResponses, addPrayerResponseReducer, updatePrayerResponseReducer, removePrayerResponseReducer  } = prayerResponseSlice.actions;

export default prayerResponseSlice.reducer;
