import axios from '@/libs/api.request'

export const getTableData = () => {
  return axios.request({
    url: 'get_table_data',
    method: 'get'
  })
}

export const getDragList = () => {
  return axios.request({
    url: 'get_drag_list',
    method: 'get'
  })
}

export const errorReq = () => {
  return axios.request({
    url: 'error_url',
    method: 'post'
  })
}

export const saveErrorLogger = info => {
  return axios.request({
    url: 'save_error_logger',
    data: info,
    method: 'post'
  })
}

export const uploadImg = formData => {
  return axios.request({
    url: 'image/upload',
    data: formData
  })
}

export const getOrgData = () => {
  return axios.request({
    url: 'get_org_data',
    method: 'get'
  })
}

export const getTreeSelectData = () => {
  return axios.request({
    url: 'get_tree_select_data',
    method: 'get'
  })
}

export const getRealtimeData = () => {
  return axios.request({
    url: 'realtime_data',
    method: 'get'
  })
}

export const getMonitorTypes = () => {
  return axios.request({
    url: 'monitor_types',
    method: 'get'
  })
}

export const getHistoryData = ({ monitorTypes, start, end }) => {
  return axios.request({
    url: 'history_data',
    method: 'get',
    params: {
      monitorTypes, start, end
    }
  })
}
