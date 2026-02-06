import { TextSimplified } from "./text-simplified.model";

export type OriginalText = {
  engineLLM: string;
  textVersion: number;
  hashCode: string;
  title: string;
  // textSimplified: TextSimplified[];
  textSimplified: TextSimplified;
  hashMatch: boolean;
}
