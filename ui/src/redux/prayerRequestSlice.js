import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';
import i18next from 'i18next';

export const submitPrayerRequest = createAsyncThunk(
  'api/createPrayer',
  async (prayerRequestForm, {rejectWithValue}) => {
    try {
      const res = await fetch('/api/prayers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Csrf-Token' : getCsrfTokenCookie()
        },
        body: JSON.stringify(prayerRequestForm),
        credentials: 'include'
      })

      if (!res.ok) {
        return rejectWithValue({error: i18next.t('failure.prayer.request')});
      }

      return await res.json();
    } catch (error) {
      return rejectWithValue({error: error.message});
    }
  }
);

export const updatePrayerRequest = createAsyncThunk(
  'api/updatePrayer',
  async ({prayerId, prayerRequestForm}, {rejectWithValue}) => {
    try {
      const res = await fetch(`/api/prayers/${prayerId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Csrf-Token' : getCsrfTokenCookie()
        },
        body: JSON.stringify(prayerRequestForm),
        credentials: 'include'
      });

      if (!res.ok) {
        return rejectWithValue({error: i18next.t('failure.update.prayer.request')});
      }

      return await res.json();
    } catch (error) {
      return rejectWithValue({error: error.message});
    }
  }
);

export const deletePrayerRequest = createAsyncThunk(
  'api/deletePrayer',
  async (prayerId, {rejectWithValue}) => {
    try {
      const res = await fetch(`/api/prayers/${prayerId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Csrf-Token' : getCsrfTokenCookie()
        },
        credentials: 'include'
      });

      if (!res.ok) {
        return rejectWithValue({error: i18next.t('failure.delete.prayer.request')});
      }
      return { prayerId };
    } catch (error) {
      return rejectWithValue({error: error.message});
    }
  }
);

export const prayerRequestSlice = createSlice({
  name: 'prayerRequest',
  initialState: {
    response: null,
  },
  reducers: {
    clearPrayerRequestResponse: (state) => {
      state.response = null;
    }
  },
  extraReducers: (builder) => {
    builder.addCase(submitPrayerRequest.fulfilled, (state) => {
      state.response = {
        'success' : i18next.t('successful.prayer.request')
      }
    });
    builder.addCase(submitPrayerRequest.rejected, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(updatePrayerRequest.fulfilled, (state) => {
      state.response = {
        'success' : i18next.t('successful.update.prayer.request')
      }
    });
    builder.addCase(updatePrayerRequest.rejected, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(deletePrayerRequest.fulfilled, (state) => {
      state.response = {
        'success': i18next.t('successful.delete.prayer.request')
      }
    });
    builder.addCase(deletePrayerRequest.rejected, (state, action) => {
      state.response = action.payload;
    });
  }
});

export const {clearPrayerRequestResponse} = prayerRequestSlice.actions;
export default prayerRequestSlice.reducer;
