export * from './authentication.service';
import { AuthenticationService } from './authentication.service';
export * from './authentication.serviceInterface';
export * from './jobController.service';
import { JobControllerService } from './jobController.service';
export * from './jobController.serviceInterface';
export const APIS = [AuthenticationService, JobControllerService];
