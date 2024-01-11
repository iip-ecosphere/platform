import { Injectable } from '@angular/core';
import { AAS_OP_PREFIX_SME, AAS_TYPE_STRING, ApiService, IDSHORT_OPERATION_PLATFORM_RESOLVESEMANTICID, IDSHORT_SUBMODEL_PLATFORM } from './api.service';
import { UtilsService } from './utils.service';

/**
 * Resolution of semantic ids via the platform/ApiService.
 */
@Injectable({
  providedIn: 'root'
})
export class SemanticResolutionService extends UtilsService {

  constructor(private api: ApiService) { 
    super();
  }

  /**
   * Resolves a semantic id via the platform. This function returns the (preferred) name and the
   * description, if possible in the given preferred language (or English) as fallback.
   * 
   * @param semanticId the semantic id to resolve 
   * @param preferredLang the preferred language to use (e.g., "en", LANG_ENGLISH)
   * @returns a semanticId object, may be filled with null values if not resolved
   */
  public async resolveSemanticId(semanticId: string | null, preferredLang:string): Promise<ResolvedSemanticId> {
    let result = {name: null, description: null} as ResolvedSemanticId;
    if (semanticId && semanticId.length > 0) {
      let input = []; 
      input.push(ApiService.createAasOperationParameter("semanticId", AAS_TYPE_STRING, semanticId));
      const response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_PLATFORM, 
        AAS_OP_PREFIX_SME + IDSHORT_OPERATION_PLATFORM_RESOLVESEMANTICID, input);
      const tmp = this.api.getPlatformResponse(response);
      if (tmp && tmp.result) {
        let tmpResult = JSON.parse(tmp.result);
        let naming;
        if (tmpResult.naming[preferredLang]) {
          naming = tmpResult.naming[preferredLang];
        } else {
          naming = tmpResult.naming.en;
        }
        result.name = naming.name;
        if (naming.description) {
          result.description = naming.description;
        }
      }
    }
    return result;
    /*const response = await this.api.executeFunction(
      "",
      "/aas/submodels/platform/submodel/submodelElements/",
      "resolveSemanticId",
      input) as platformResponse;*/
    //return this.getSemanticInfo(response);
  }

    // Returns an array [name, description]
/*    public getSemanticInfo(response:platformResponse) {
      let return_value = [null, null];
      if(response && response.outputArguments) {
        let output = response.outputArguments[0]?.value?.value;
        if (output) {
          let temp = JSON.parse(output);
          if (temp.result) {
            let result = JSON.parse(temp.result);
            if (result.naming.en.description) {
              return_value = [result.naming.en.name, result.naming.en.description]
            } else {
              return_value = [result.naming.en.name, null]
            }
          }
        }
      }
      return return_value
    }*/

    /**
     * Resolves a chunk of semantic ids. The preferred language is used if possible,
     * else English is used as fallback. Although implemented as loop over functions of this 
     * service, the implementation may be improved in future by additional AAS operations. Thus,
     * please use this function where applicable.
     * 
     * @param semanticIds the semantic ids to resolve
     * @param preferredLang the preferred language to use (e.g., "en", LANG_ENGLISH)
     * @returns the resolved semantic ids, may be entires with null values if not resolvable
     */
    public async resolveSemanticIds(semanticIds: string[], preferredLang: string): Promise<ResolvedSemanticId[]> {
      let result = [] as ResolvedSemanticId[];
      for (const semId of semanticIds) {
        result.push(await this.resolveSemanticId(semId, preferredLang));
      }
      return result;
    }

    /**
     * Turns a potentially absent semanticId into a string handled by this service.
     * 
     * @param semanticId the semanticid or null 
     * @returns the semantic id
     */
    public static validateId(semanticId: string | null | undefined) : string {
      if (semanticId) {
        return semanticId; 
      } else {
        return SEMID_EMPTY;
      }
    }

}

export const SEMID_EMPTY = "";
export const LANG_ENGLISH = "en";
export const LANG_GERMAN = "de";

export interface ResolvedSemanticId {
  name: string | null,
  description: string | null
}