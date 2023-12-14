// https://stackoverflow.com/questions/38582293/how-to-declare-a-variable-in-a-template-in-angular

import {
    Directive,
    EmbeddedViewRef,
    Input,
    TemplateRef,
    ViewContainerRef,
  } from '@angular/core';
  
  @Directive({
    selector: '[ngVar]',
  })
  export class NgVar<T = unknown> {
    private _context: NgVarContext<T> = new NgVarContext<T>();
    private _viewRef: EmbeddedViewRef<NgVarContext<T>> | null = null;
  
    @Input()
    set ngVar(context: T) {
      this._context.$implicit = this._context.ngVar = context;
  
      if (!this._viewRef) {
        this._viewRef = this._viewContainer.createEmbeddedView(
          this._templateRef,
          this._context
        );
      }
    }
  
    // https://github.com/angular/angular/blob/main/packages/common/src/directives/ng_if.ts
  
    /**
     * Assert the correct type of the expression bound to the `ngIf` input within the template.
     *
     * The presence of this static field is a signal to the Ivy template type check compiler that
     * when the `NgIf` structural directive renders its template, the type of the expression bound
     * to `ngIf` should be narrowed in some way. For `NgIf`, the binding expression itself is used to
     * narrow its type, which allows the strictNullChecks feature of TypeScript to work with `NgIf`.
     */
  
    static ngTemplateGuard_ngVar: 'binding';
  
    /**
     * Asserts the correct type of the context for the template that `NgIf` will render.
     *
     * The presence of this method is a signal to the Ivy template type-check compiler that the
     * `NgIf` structural directive renders its template with a specific context type.
     */
  
    // Passing down variable Type
    static ngTemplateContextGuard<T>(
      dir: NgVar<T>,
      ctx: any
    ): ctx is NgVarContext<T> {
      return true;
    }
  
    constructor(
      private _viewContainer: ViewContainerRef,
      private _templateRef: TemplateRef<NgVarContext<T>>
    ) {}
  }
  
  export class NgVarContext<T = unknown> {
    public $implicit: T = null!;
    public ngVar: T = null!;
  }