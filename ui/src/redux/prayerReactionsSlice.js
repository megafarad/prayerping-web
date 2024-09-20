import {createSlice, createAsyncThunk} from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';

export const fetchPrayerReactions = createAsyncThunk(
  'api/prayerReactions',
  async ({prayerId}) => {
    const response = await fetch(`/api/prayers/${prayerId}/reactions`);
    const data = await response.json();
    return { reactions: data, prayerId };
  }
);

export const reactToPrayer = createAsyncThunk(
  'api/reactToPrayer',
  async ({prayerId, form}) => {
    const response = await fetch(`/api/prayers/${prayerId}/reactions`, {
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

export const deletePrayerReaction = createAsyncThunk(
  'api/deletePrayerReaction',
  async ({prayerId, reactionId}) => {
    await fetch(`/api/prayers/${prayerId}/reactions/${reactionId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include'
    });
    return { prayerId, reactionId };
  }
);

const prayerReactionsSlice = createSlice({
  name: 'prayerReactions',
  initialState: {
    reactions: { }
  },
  reducers: {
    addPrayerReaction: (state, action) => {
      if (!state.reactions[action.payload.requestId]) {
        state.reactions[action.payload.requestId] = [];
      }
      const existingIdx = state.reactions[action.payload.requestId].findIndex(reaction => reaction.id === action.payload.id);
      if (existingIdx === -1) {
        state.reactions[action.payload.requestId].push(action.payload);
      } else {
        state.reactions[action.payload.requestId][existingIdx] = action.payload;
      }
    },
    removePrayerReaction: (state, action) => {
      if (!state.reactions[action.payload.requestId]) {
        state.reactions[action.payload.requestId] = [];
      }
      state.reactions[action.payload.requestId] = state.reactions[action.payload.requestId]
        .filter(reaction => reaction.id !== action.payload.id);
    }
  },
  extraReducers: (builder) => {
    builder.addCase(fetchPrayerReactions.fulfilled, (state, action) => {
      state.reactions[action.payload.prayerId] = action.payload.reactions;
    });
    builder.addCase(reactToPrayer.fulfilled, (state, action) => {
      if (!state.reactions[action.payload.requestId]) {
        state.reactions[action.payload.requestId] = [];
      }
      const existingIdx = state.reactions[action.payload.requestId].findIndex(reaction => reaction.id === action.payload.id);

      if (existingIdx === -1) {
        state.reactions[action.payload.requestId].push(action.payload);
      } else {
        state.reactions[action.payload.requestId][existingIdx] = action.payload;
      }
    });
    builder.addCase(deletePrayerReaction.fulfilled, (state, action) => {
      const { prayerId, reactionId } = action.payload;
      if (state.reactions[prayerId]) {
        state.reactions[prayerId] = state.reactions[prayerId].filter(
          (reaction) => reaction.id !== reactionId
        );
      }
    });
  }
});

export const { addPrayerReaction, removePrayerReaction } = prayerReactionsSlice.actions
export default prayerReactionsSlice.reducer;
