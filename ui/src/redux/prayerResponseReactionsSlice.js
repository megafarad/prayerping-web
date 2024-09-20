import {createSlice, createAsyncThunk} from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';

export const fetchPrayerResponseReactions = createAsyncThunk(
  'api/prayerResponseReactions',
  async ({responseId}) => {
    const fetchResponse = await fetch(`/api/responses/${responseId}/reactions`);
    const data = await fetchResponse.json();
    return { reactions: data, responseId };
  });

export const reactToResponse = createAsyncThunk(
  'api/reactToResponse',
  async ({responseId, form}) => {
    const fetchResponse = await fetch(`/api/responses/${responseId}/reactions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
      body: JSON.stringify(form)
    });

    return await fetchResponse.json();
  }
);

export const deletePrayerResponseReaction = createAsyncThunk(
  'api/deletePrayerResponseReaction',
  async ({responseId, reactionId}) => {
    await fetch(`/api/responses/${responseId}/reactions/${reactionId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include'
    });
    return { responseId, reactionId }
  }
);

const prayerResponseReactionsSlice = createSlice({
  name: 'prayerResponseReactions',
  initialState: {
    reactions: { }
  },
  reducers: {
    addPrayerResponseReaction: (state, action) => {
      if (!state.reactions[action.payload.responseId]) {
        state.reactions[action.payload.responseId] = [];
      }
      const existingIdx = state.reactions[action.payload.responseId].findIndex(reaction => reaction.id === action.payload.id);
      if (existingIdx === -1) {
        state.reactions[action.payload.responseId].push(action.payload);
      } else {
        state.reactions[action.payload.responseId][existingIdx] = action.payload;
      }
    },
    removePrayerResponseReaction: (state, action) => {
      if (!state.reactions[action.payload.responseId]) {
        state.reactions[action.payload.responseId] = [];
      }
      state.reactions[action.payload.requestId] = state.reactions[action.payload.responseId]
        .filter(reaction => reaction.id !== action.payload.id);
    }
  },
  extraReducers: (builder) => {
    builder.addCase(fetchPrayerResponseReactions.fulfilled, (state, action) => {
      state.reactions[action.payload.responseId] = action.payload.reactions;
    });
    builder.addCase(reactToResponse.fulfilled, (state, action) => {
      if (!state.reactions[action.payload.responseId]) {
        state.reactions[action.payload.responseId] = [];
      }
      const existingIdx = state.reactions[action.payload.responseId]
        .findIndex(reaction => reaction.id === action.payload.id);
      if (existingIdx === -1) {
        state.reactions[action.payload.responseId].push(action.payload);
      }
    });
    builder.addCase(deletePrayerResponseReaction.fulfilled, (state, action) => {
      const { responseId, reactionId } = action.payload;
      if (state.reactions[responseId]) {
        state.reactions[responseId] = state.reactions[responseId].filter(
          (reaction) => reaction.id !== reactionId
        );
      }
    });
  }
});

export const {addPrayerResponseReaction,removePrayerResponseReaction} = prayerResponseReactionsSlice.actions
export default prayerResponseReactionsSlice.reducer;
