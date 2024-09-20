import {createSlice, createAsyncThunk} from '@reduxjs/toolkit';

export const fetchPrayerFeed = createAsyncThunk(
  'api/prayerFeed',
  async ({page, allPrayers}) => {
    const response = allPrayers ? await fetch(`/api/prayers?page=${page}`) : await
      fetch(`/api/feed?page=${page}`);
    const data = await response.json();
    const lastPage = data.items.length + data.offset === data.total;
    return { prayers: data.items, page, hasMore: !lastPage };
  }
);

const prayerFeedSlice = createSlice({
  name: 'prayers',
  initialState: {
    prayers: [],
    page: 0,
    status: 'idle',
    error: null,
    hasMore: true
  },
  reducers: {
    resetPrayerFeed: (state) => {
      state.prayers = [];
      state.page = 0;
      state.hasMore = true;
    },
    addPrayer: (state, action) => {
      state.prayers.unshift(action.payload);
    },
    updatePrayer: (state, action) => {
      const updatedPrayer = action.payload;
      const idx = state.prayers.findIndex(prayer => prayer.id === updatedPrayer.id);
      if (idx !== -1) {
        state.prayers[idx] = updatedPrayer;
      }
    },
    deletePrayer: (state, action) => {
      state.prayers = state.prayers.filter(prayer => prayer.id !== action.payload);
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPrayerFeed.pending, (state, action) => {
        state.status = 'loading';
      })
      .addCase(fetchPrayerFeed.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.prayers = [...state.prayers, ...action.payload.prayers];
        state.page = action.payload.page + 1;
        state.hasMore = action.payload.hasMore;
      })
      .addCase(fetchPrayerFeed.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      });
  }
});

export const { resetPrayerFeed, addPrayer,
  updatePrayer, deletePrayer  } = prayerFeedSlice.actions
export default prayerFeedSlice.reducer;
