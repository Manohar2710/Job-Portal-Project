import { CanDeactivateFn } from "@angular/router";


export interface HasUnsavedChanges {
    hasUnsavedChanges(): boolean
}

export const unSaveChangeGuard: CanDeactivateFn<HasUnsavedChanges>= (component) => {
    if(component.hasUnsavedChanges()) {
        confirm("you have unsaved changes are you sure you want to exit?")
    }
    return true;
}