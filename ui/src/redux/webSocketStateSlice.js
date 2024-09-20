import {createSlice} from '@reduxjs/toolkit';

const webSocketStateSlice = createSlice({
  name: 'webSocketState',
  initialState: {
    status: 'disconnected'
  },
  reducers: {
    setStatus: (state, action) => {
      state.status = action.payload;
    }
  }
});

export const {setStatus} = webSocketStateSlice.actions;
export default webSocketStateSlice.reducer;
