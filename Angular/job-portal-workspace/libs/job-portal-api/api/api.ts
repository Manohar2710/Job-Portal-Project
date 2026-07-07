export * from './authController.service';
import { AuthControllerService } from './authController.service';
export * from './authController.serviceInterface';
export * from './jobController.service';
import { JobControllerService } from './jobController.service';
export * from './jobController.serviceInterface';
export const APIS = [AuthControllerService, JobControllerService];
