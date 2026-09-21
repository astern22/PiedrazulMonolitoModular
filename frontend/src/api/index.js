import apiClient from './client.js'
import authService from './auth.service.js'
import specialtyService from './specialty.service.js'
import appointmentService from './appointment.service.js'

export { apiClient, authService, specialtyService, appointmentService }

export default {
  client: apiClient,
  auth: authService,
  specialties: specialtyService,
  appointments: appointmentService,
}
