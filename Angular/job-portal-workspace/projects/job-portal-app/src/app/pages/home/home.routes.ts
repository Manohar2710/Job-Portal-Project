import { Routes } from "@angular/router";
import { JobsList } from "./jobs-list/jobs-list";
import { EditJob } from "./edit-job/edit-job";
import { unSaveChangeGuard } from "auth-feature";


export const homeRoutes: Routes = [
    {
        path: '',
        children: [
            {
                path: 'jobsList', component: JobsList,
                data: {roles: ['ADMIN'], title: 'Jobs list'}

            },
            {
                path: 'edit-job/:id', component: EditJob,
                canDeactivate: [unSaveChangeGuard]
            }
        ]
    }
]