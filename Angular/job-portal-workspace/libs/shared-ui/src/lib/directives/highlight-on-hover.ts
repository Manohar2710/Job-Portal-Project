import { Directive, ElementRef, HostListener, inject, Input, Renderer2 } from '@angular/core';

@Directive({
  selector: '[uiHighlightOnHover]',
})
export class HighlightOnHover {
  private elementRef = inject(ElementRef);

  private renderer = inject(Renderer2);

  @Input() appHighlight = 'yellow';
  @Input() defaultColor = 'transparent';

  @HostListener('mouseenter')
  onMouseEnter() {
    this.setBgColor(this.appHighlight || this.defaultColor);
  }

  @HostListener('mouseleave')
  onMouseLeave() {
    this.setBgColor(this.defaultColor);
  }
  constructor() {}

  setBgColor(color: string){
    this.renderer.setStyle(
      this.elementRef.nativeElement,
      'background-color',
      color      
    )
  }
}
