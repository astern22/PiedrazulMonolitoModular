import apiClient from './client.js'
import authService from './auth.service.js'
import specialtyService from './specialty.service.js'
import appointmentService from './appointment.service.js'
import schedulingService from './scheduling.service.js'
import professionalService from './professional.service.js'
import patientService from './patient.service.js'
import userService from './user.service.js'

export {
  apiClient,
  authService,
  specialtyService,
  appointmentService,
  schedulingService,
  schedulingService as availabilityService,
  professionalService,
  patientService,
  userService,
}

export default {
  client: apiClient,
  auth: authService,
  specialties: specialtyService,
  appointments: appointmentService,
  scheduling: schedulingService,
  availability: schedulingService,
  professionals: professionalService,
  patients: patientService,
  users: userService,
}
