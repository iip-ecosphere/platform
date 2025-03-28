import { TestBed } from '@angular/core/testing';

import { LANG_ENGLISH, LANG_GERMAN, ResolvedSemanticId, SemanticResolutionService } from './semantic-resolution.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('SemanticResolutionService', () => {
  let service: SemanticResolutionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [],
    providers: [provideHttpClient(withInterceptorsFromDi())]
});
    service = TestBed.inject(SemanticResolutionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should validate non-existing id', () => {
    expect(SemanticResolutionService.validateId(null)).toBe("");
    expect(SemanticResolutionService.validateId(undefined)).toBe("");
    expect(SemanticResolutionService.validateId("")).toBe("");
    expect(SemanticResolutionService.validateId("abba")).toBe("abba");
  });

  it('should resolve with/without id', async() => {
    await expectIdResolution(service, "", LANG_ENGLISH, null, ISNULL);
    await expectIdResolution(service, "abba", LANG_ENGLISH, null, ISNULL);
    await expectIdResolution(service, "abba", LANG_GERMAN, null, ISNULL);
    await expectIdResolution(service, "0173-1#05-AAA114#003", LANG_ENGLISH, "ms", HASVALUE);
    await expectIdResolution(service, "0173-1#05-AAA114#003", LANG_GERMAN, "ms", HASVALUE);
  });

  it('should resolve multiple ids', async() => {
    expect(await service.resolveSemanticIds([], LANG_ENGLISH)).toEqual([]);
    let res = await service.resolveSemanticIds(["", "0173-1#05-AAA114#003"], LANG_ENGLISH);
    expect(res).toBeTruthy();
    expect(res.length).toBe(2);
    expectSemanticId(res[0], null, ISNULL);
    expectSemanticId(res[1], "ms", HASVALUE);
  });

});

const ISNULL = (d : string | null) => d == null;
const HASVALUE = (d : string | null) => d != null && d.length > 0;

async function expectIdResolution(service: SemanticResolutionService, semanticId: string | null, preferredLang: string, expectedName: string | null, expectedDescription: (desc: string | null) => Boolean) {
  let semId = await service.resolveSemanticId(semanticId, preferredLang);
  expectSemanticId(semId, expectedName, expectedDescription);
}

function expectSemanticId(semId: ResolvedSemanticId, expectedName: string | null, expectedDescription: (desc: string | null) => Boolean) {
  expect(semId).toBeTruthy();
  expect(semId.name).toBe(expectedName);
  expect(expectedDescription(semId.description)).toBeTrue();
}