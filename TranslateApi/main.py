from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import MBartForConditionalGeneration, MBart50TokenizerFast

model = MBartForConditionalGeneration.from_pretrained("facebook/mbart-large-50-many-to-many-mmt")
tokenizer = MBart50TokenizerFast.from_pretrained("facebook/mbart-large-50-many-to-many-mmt")

app = FastAPI()


class Item(BaseModel):
    text: str
    input_lang: str
    output_lang: str


# 该方法用于将text文本从input_lang语言翻译成output_lang语言
# 各个语言对应的ID见https://huggingface.co/facebook/mbart-large-50-many-to-many-mmt
def translate_text(text: str, input_lang: str, output_lang: str):
    tokenizer.src_lang = input_lang
    encoded_in = tokenizer(text, return_tensors="pt")
    generated_tokens = model.generate(
        **encoded_in,
        forced_bos_token_id=tokenizer.lang_code_to_id[output_lang]
    )
    result = tokenizer.batch_decode(generated_tokens, skip_special_tokens=True)
    return str(result).strip('][\'')


@app.post("/translate/")
async def predict(item: Item):
    result = item.text
    if item.input_lang != "en_XX":
        result = translate_text(result, item.input_lang, "en_XX")
    if item.output_lang != "en_XX":
        result = translate_text(result, "en_XX", item.output_lang)
    return {"result": result}
