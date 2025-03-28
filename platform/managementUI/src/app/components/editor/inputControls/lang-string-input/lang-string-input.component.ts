import { Component, Input, OnInit } from '@angular/core';
import { DataUtils } from 'src/app/services/utils.service';
import { Resource, editorInput } from 'src/interfaces';

/**
 * An AAS/OPC-UA lang string input component, returning the simplified form of text@lang as 
 * usual in the configuration model.
 */
@Component({
    selector: 'app-lang-string-input',
    templateUrl: './lang-string-input.component.html',
    styleUrls: ['./lang-string-input.component.scss'],
    standalone: false
})
export class LangStringInputComponent implements OnInit {

  @Input() input: editorInput | undefined
  @Input() meta: Resource | undefined

  langEnum: string[] = [];
  langSelected: string = ""; 
  textInput: string = "";

  constructor() { }

  /**
   * Returns the default language as one of those provided by getLangs() and in langEnum();
   * 
   * @returns the default language
   */
  private getDefaultLang() {
    return this.getLangs()[0]; // TODO set to current locale
  }

  /**
   * Returns the available languages.
   * 
   * @returns the languages
   */
  private getLangs() {
    return ["de", "en"]; // TODO fill, may become (country, lang) to be projected below
  }

  /**
   * Initializes this component.
   */
  ngOnInit(): void {
    this.langEnum = this.getLangs();
    let defaultLang = this.getDefaultLang();
    this.langSelected = defaultLang;
    if (this.input) {
      let tmp = String(this.input.value); // input.value is of type any
      this.langSelected = DataUtils.getLangStringLang(tmp) || defaultLang;
      this.textInput = DataUtils.getLangStringText(tmp);
    }
  }

  /**
   * Called when text input or lang selection changes.
   * 
   * @param event the change event
   */
  inputChanged(event: any) {
    if (this.input && this.textInput && this.langSelected) {
      this.input.value = DataUtils.composeLangString(this.textInput, this.langSelected);
    }
  }

}
