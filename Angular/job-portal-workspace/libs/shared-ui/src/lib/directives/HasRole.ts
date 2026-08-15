import { Directive, inject, Input, TemplateRef, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[uiHasRole]'
})
export class HasRole {
  private templateRef = inject(TemplateRef<unknown>);
  private container = inject(ViewContainerRef);
  private hasView = false;
  constructor() {}

  @Input() set uiHasRole(condition: boolean) {
    if(condition && !this.hasView) {
      this.container.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if(!condition && !this.hasView) {
      this.container.clear();
      this.hasView = false;
    }
  }
}
